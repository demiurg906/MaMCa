package org.physics.mamca

import org.apache.commons.cli.*
import org.apache.commons.io.FileUtils
import org.physics.mamca.math.Vector
import org.physics.mamca.math.abs
import org.physics.mamca.math.rank
import org.physics.mamca.util.Logger
import org.physics.mamca.util.eFormat
import org.physics.mamca.util.format
import org.physics.mamca.util.playSuccessNotification
import java.io.File
import java.io.IOException
import java.util.*

val prop = Properties()

fun main(args: Array<String>) {
    val options = Options()

    // settings
    options.addOption(Option.
            builder("s").
            hasArg().
            required().
            longOpt("settings").
            desc("path to json settings file").
            build()
    )
    //Mathematica path
    options.addOption(Option.
            builder("m").
            hasArg().
            required().
            longOpt("mathematica").
            desc("path to Mathematica executable").
            build()
    )

    val parser: CommandLineParser = DefaultParser()
    val formatter: HelpFormatter = HelpFormatter()
    val cmd: CommandLine

    try {
        cmd = parser.parse(options, args)
    } catch (e: ParseException) {
        System.out.println(e.message)
        formatter.printHelp("utility-name", options)

        System.exit(1)
        return
    }

    prop.setProperty("MATHEMATICA_PATH", cmd.getOptionValue("mathematica"))

    val settingsFile = cmd.getOptionValue("settings")
    val settings = loadSettingsFromJson(settingsFile)

    prepareFolders(settings)
    createSettingsJson("${settings.dataFolder}/${settings.name}/settings.json", settings)
    rescaleSettingsFields(settings)

    val startTime = System.currentTimeMillis()
    val midTime: Long
    if (!settings.hysteresis) {
        midTime = singleRun(settings)
    } else {
        midTime = hysteresisRun(settings)
    }
    val endTime = System.currentTimeMillis()

    fun timeFormat(time: Long): String {
        val seconds = time / 1000.0
        if (seconds < 60)
            return "${seconds.format(2)} seconds"
        val minutes = seconds / 60.0
        if (seconds < 60)
            return "${minutes.format(2)} minutes"
        val hours = minutes / 60.0
        return "${hours.format(2)} hours"
    }

    Logger.addDelimiter().
            info("time of computation is ${timeFormat(endTime - midTime)}").
            info("time of working is ${timeFormat(endTime - startTime)}").
            addDelimiter()
    File("${settings.dataFolder}/${settings.name}/log.log").printWriter().use { out ->
        out.write(Logger.toString())
    }

    playSuccessNotification()
}

/**
 * переводит поля настроек в единицы измерения, используемые в модели
 */
fun rescaleSettingsFields(settings: Settings) {
//    settings.kan /= EV_TO_DJ
//    settings.kan /= NM3_TO_M3
    settings.relative_precision *= PERCENT_COEFFICIENT
    settings.jex /= EV_TO_DJ
    settings.time *= S_TO_NS
    settings.b_x *= OE_TO_TESLA
    settings.b_y *= OE_TO_TESLA
    settings.b_z *= OE_TO_TESLA
}

/**
 * создает все выходные папки, если их нет
 */
fun prepareFolders(settings: Settings) {
    val dataFolder = File("${settings.dataFolder}/${settings.name}")
    val outFolders = mutableListOf(
            "${settings.dataFolder}/${settings.name}/out",
            "${settings.dataFolder}/${settings.name}/pictures/moments"
    ).map(::File)

    fun clearFolders() {
        try {
            if (dataFolder.exists()) {
                if (dataFolder.isDirectory) {
                    FileUtils.cleanDirectory(dataFolder)
                } else if (dataFolder.isFile) {
                    dataFolder.delete()
                }
            }
        } catch (e: IOException) {
            if (e.message == null) {
                throw e
            }
            println(e.message)
            System.exit(1)
        }
    }

    fun createFolders() {
        outFolders.filterNot(File::exists).forEach { it.mkdirs() }
    }

    fun checkFolders(): Boolean {
        return outFolders.all(File::exists)
    }

    var done = false
    for (i in 1..5) {
        try {
            clearFolders()
            createFolders()
            done = checkFolders()
            if (done) {
                break
            }
        } catch (e: IOException) {
//            не удалось создать, пытаемся еще раз
        }
    }

    if (!done) {
        println("Creation of out directories failed.")
        System.exit(1)
    }
}

/**
 * Запускает один цикл симуляции
 */
fun singleRun(settings: Settings): Long {
    val dataFolder ="${settings.dataFolder}/${settings.name}"
    val outFolder = File("$dataFolder/out")

    val sample = Sample(settings)
    val midTime = System.currentTimeMillis()
    sample.dumpToJsonFile(outFolder.canonicalPath, "sample.json")
    sample.saveState(outFolder = outFolder.canonicalPath, filename = "momenta_00_1_${0.0.format(9)}.txt")

    val (startEnergies, endEnergies, numberOfSteps) = sample.processModel(outFolder.canonicalPath)
    val startEnergy = startEnergies.first * DJ_TO_EV
    val endEnergy = endEnergies.first * DJ_TO_EV

    Logger.info("diff between enerfies is ${(startEnergy - endEnergy).eFormat()}")
            .addLineBreak()
            .info("number of simulation steps is $numberOfSteps")
            .info("number of jumps is ${sample.nJumps}")

    if (endEnergy > startEnergy) {
        Logger.info("\nWOOOOOOOOOOW\n")
    }
    Logger.addDelimiter()
    return midTime
}

fun hysteresisRun(settings: Settings): Long {
    val dataFolder ="${settings.dataFolder}/${settings.name}"
    val outFolder = File("$dataFolder/out")
    FileUtils.cleanDirectory(outFolder)


    val k = settings.hysteresisSteps
    val n = settings.hysteresisDenseSteps * settings.hysteresisDenseMultiplier

    val numberOfSteps = 2 * (n + k) - 1

    val maxB = Vector(settings.b_x, settings.b_y, settings.b_z)
    val borderB = maxB * settings.hysteresisDenseSteps / settings.hysteresisSteps

    val bLinStep = maxB / k
    val bDenseStep = bLinStep / settings.hysteresisDenseMultiplier

    val defaultTime = settings.time
    val denseTime = settings.time / settings.hysteresisDenseMultiplier

    var stepIndex = 1
    val digitsOfIndex = rank(numberOfSteps)

    val sample = Sample(settings)
    sample.dumpToJsonFile(outFolder.canonicalPath, "sample.json")
    sample.saveState(outFolder = outFolder.canonicalPath, filename = "momenta_${0.format(digitsOfIndex)}_fst_0.0_0.0_0.0.txt")

    val midTime = System.currentTimeMillis()

    fun step(inc: Boolean, direction: String) {
        val stepVal: Vector
        if (abs(sample.b) < abs(borderB)) {
            stepVal = bDenseStep
            settings.time = denseTime
        } else {
            stepVal = bLinStep
            settings.time = defaultTime
        }

        if (inc) {
            sample.b += stepVal
        } else {
            sample.b -= stepVal
        }

        if (abs(sample.b) > abs(maxB)) {
            return
        }

        Logger.info("b: ${sample.b}").
                info("step: $stepVal").
                info("i: $stepIndex")
        sample.processModel()
        Logger.addDelimiter()
        val field = listOf(sample.b.x, sample.b.y, sample.b.z).map { it * TESLA_TO_OE }.map { it.format(3) }
        sample.saveState(
                outFolder.canonicalPath,
                "momenta_${stepIndex.format(digitsOfIndex)}_${direction}_${field[0]}_${field[1]}_${field[2]}.txt")
        stepIndex += 1
    }

    println("Hysteresis cycle started")
    println("There will be ${5 * numberOfSteps / 2} steps")
    println("")

    sample.b = Vector()
    sample.processModel()

    println("Magnetic field began to increase")
    println("Wait ${numberOfSteps / 2} steps")
    println("")

    for (i in numberOfSteps / 2 until numberOfSteps) {
        step(true, "fst")
    }

    sample.b = maxB
    println("Magnetic field began to decline")
    println("Wait $numberOfSteps steps")
    println("")

    for (i in 1..numberOfSteps) {
        step(false, "neg")
    }

    sample.b = -maxB
    println("Magnetic field began to increase")
    println("Wait $numberOfSteps steps")
    println("")

    for (i in 1..numberOfSteps) {
        step(true, "pos")
    }

    println("Generation of hysteresis cycle complete")

    return midTime
}
