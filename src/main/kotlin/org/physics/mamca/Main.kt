package org.physics.mamca

import org.apache.commons.cli.*
import org.apache.commons.io.FileUtils
import org.physics.mamca.math.Vector
import org.physics.mamca.math.abs
import org.physics.mamca.math.rank
import org.physics.mamca.util.*
import java.io.File
import java.io.IOException


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

    val settingsFile = cmd.getOptionValue("settings")
    val settings = loadSettingsFromJson(settingsFile)

    prepareFolders(settings)
    createSettingsJson("${settings.dataFolder}/${settings.name}/settings.json", settings)

    val startTime = System.currentTimeMillis()
    val midTime: Long
    if (!settings.hysteresis) {
        midTime = singleRun(settings)
    } else {
        midTime = hysteresisRun(settings)
    }
    val endTime = System.currentTimeMillis()
    Logger.addDelimiter().
            info("time of working is ${(endTime - startTime) / 1000.0} seconds").
            info("time of computation is ${(endTime - midTime) / 1000.0} seconds").
            addDelimiter()
    File("${settings.dataFolder}/${settings.name}/log.log").printWriter().use { out ->
        out.write(Logger.toString())
    }

    Sound.playSound("./resources/notifications/arpeggio.wav").join()
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
    val startEnergy = startEnergies.first / EV_TO_DJ
    val endEnergy = endEnergies.first / EV_TO_DJ
//    sample.saveState(outFolder = outFolder.canonicalPath, filename = settings.momentaFileName)


    Logger.addLineBreak().addDelimiter()
    Logger.info("sample size is ${settings.x}x${settings.y}x${settings.z} with ${settings.n} particles per ring")
    Logger.info("total number of particles is ${settings.x * settings.y * settings.z * settings.n}")
    Logger.info("")
    Logger.info("full energy on start is ${startEnergy.eFormat()} eV")
    Logger.info("energies on start is ${formatEnergies(startEnergies.second)} eV")
    Logger.info("full energy on end is ${endEnergy.eFormat()} eV")
    Logger.info("energies on end is ${formatEnergies(endEnergies.second)} eV")
    Logger.info("diff between enerfies is ${(startEnergy - endEnergy).eFormat()}")
    Logger.info("")
    Logger.info("number of simulation steps is $numberOfSteps")
    Logger.info("")
    Logger.info("number of jumps is ${sample.nJumps}")

    if (endEnergy > startEnergy) {
        Logger.info("\nWOOOOOOOOOOW\n")
    }
    Logger.addDelimiter()
    return midTime
}

fun hysteresisRun(settings: Settings): Long {
    val startTime = System.currentTimeMillis()

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
                info("i: $stepIndex").
                addDelimiter()
        sample.processModel()
        sample.saveState(
                outFolder.canonicalPath,
                "momenta_${stepIndex.format(digitsOfIndex)}_${direction}_${sample.b.x.format(3)}_${sample.b.y.format(3)}_${sample.b.z.format(3)}.txt")
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
    val endTime = System.currentTimeMillis()
    println("time of working is ${(endTime - startTime) / 1000.0} seconds")

    return midTime
}
