package org.physics.mamca

import org.apache.commons.cli.*
import org.apache.commons.io.FileUtils
import org.physics.mamca.math.Vector
import org.physics.mamca.math.div
import org.physics.mamca.math.log
import org.physics.mamca.util.*
import java.io.File


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

    if (!settings.hysteresis) {
        singleRun(settings)
    } else {
        hysteresisRun(settings)
    }

    Sound.playSound("./resources/notifications/arpeggio.wav").join()
}


/**
 * создает все выходные папки, если их нет
 */
fun prepareFolders(settings: Settings) {
    var outFolderPath =  "${settings.dataFolder}/${settings.name}/out"
    if (settings.hysteresis) {
        outFolderPath += "/hyst"
    }
    val outFolder = File(outFolderPath)
    if (!outFolder.exists()) {
        outFolder.mkdirs()
    }
}

/**
 * Запускает один цикл симуляции
 */
fun singleRun(settings: Settings) {
    val startTime = System.currentTimeMillis()

    val dataFolder ="${settings.dataFolder}/${settings.name}"
    val outFolder = File("$dataFolder/out")

    val sample = Sample(settings)
    sample.dumpToJsonFile(outFolder.canonicalPath, "sample.json")
    sample.saveState(outFolder = outFolder.canonicalPath, filename = "momenta.at_start.txt")

    val midTime = System.currentTimeMillis()
    val (startEnergies, endEnergies, numberOfSteps) = sample.processModel()
    val startEnergy = startEnergies.first / EV_TO_DJ
    val endEnergy = endEnergies.first / EV_TO_DJ
    sample.saveState(outFolder = outFolder.canonicalPath, filename = settings.momentaFileName)

    val endTime = System.currentTimeMillis()


    Logger.addLineBreak().addDelimiter()
    Logger.info("time of working is ${(endTime - startTime) / 1000.0} seconds")
    Logger.info("time of computation is ${(endTime - midTime) / 1000.0} seconds")
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

    val log = Logger.toString()
    print(log)
    File("$dataFolder/log.log").printWriter().use { out ->
        out.write(log)
    }
}

fun hysteresisRun(settings: Settings) {
    val startTime = System.currentTimeMillis()

    val dataFolder ="${settings.dataFolder}/${settings.name}"
    val outFolder = File("$dataFolder/out")
    val outHystFolder = File("$dataFolder/out/hyst")
    FileUtils.cleanDirectory(outHystFolder)

    val sample = Sample(settings)
    sample.dumpToJsonFile(outFolder.canonicalPath, "sample.json")
    sample.saveState(outFolder = outFolder.canonicalPath, filename = "momenta_at_start.txt")

    val k = settings.hysteresisSteps
    val n = (2.5 * log(1 / settings.hysteresisLogScale, 1 + 4.0 / k)).toInt()

    val numberOfSteps = 2 * (n + k) - 1

    val maxB = Vector(settings.b_x, settings.b_y, settings.b_z)
    var minLogB = maxB * settings.hysteresisLogScale
    val bLinStep = minLogB / k
    minLogB = switchZerosToOnes(minLogB)
    val bLogStep = switchZerosToOnes(maxB / minLogB) % (1.0 / (n - 1))

    fun step(i: Int, inc: Boolean, direction: String) {
        if (i < n - 1) {
            settings.viscosity = 0.5
            sample.b /= bLogStep
        } else if (i in n - 1 until n - 1 + 2 * k) {
            settings.viscosity = 0.05
            if (inc) {
                sample.b += bLinStep
            } else {
                sample.b -= bLinStep
            }
        } else {
            settings.viscosity = 0.5
            sample.b /= (1.0 / bLogStep) // костыльное поэлементное перемножение двух векторов
        }
        sample.processModel()
        sample.saveState(outHystFolder.canonicalPath, "hyst,$direction,${sample.b.x.format(2)},${sample.b.y.format(2)},${sample.b.z.format(2)}.txt")
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
        step(i, true, "fst")
    }

    sample.b = maxB
    println("Magnetic field began to decline")
    println("Wait $numberOfSteps steps")
    println("")

    for (i in 1..numberOfSteps) {
        step(i, false, "neg")
    }

    sample.b = -maxB
    println("Magnetic field began to increase")
    println("Wait $numberOfSteps steps")
    println("")

    for (i in 1..numberOfSteps) {
        step(i, true, "pos")
    }

    println("Generation of hysteresis cycle complete")
    val endTime = System.currentTimeMillis()
    println("time of working is ${(endTime - startTime) / 1000.0} seconds")
}

fun switchZerosToOnes(v: Vector): Vector {
    var res = v
    if (equalsDouble(res.x, 0.0))
        res += Vector(1.0, 0.0, 0.0)
    if (equalsDouble(res.y, 0.0))
        res += Vector(0.0, 1.0, 0.0)
    if (equalsDouble(res.z, 0.0))
        res += Vector(0.0, 0.0, 1.0)
    return res
}