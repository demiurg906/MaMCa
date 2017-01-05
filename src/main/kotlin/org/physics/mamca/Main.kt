package org.physics.mamca

import org.apache.commons.cli.*
import org.physics.mamca.util.eFormat
import org.physics.mamca.util.formatEnergies


fun main(args: Array<String>) {
    val options = Options()

    // settings
    options.addOption(Option.
            builder("s").
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

    if (!settings.hysteresis) {
        singleRun(settings)
    } else {
        hysteresisRun(settings)
    }
}


/**
 * Запускает один цикл симуляции
 */
fun singleRun(settings: Settings) {
    val startTime = System.currentTimeMillis()

    val sample = Sample(settings)
    sample.dumpToJsonFile(settings.outFolder, "sample.json")
    sample.saveState(outFolder = settings.outFolder, filename = "momenta_before.txt")

    val midTime = System.currentTimeMillis()
    val (startEnergies, endEnergies, numberOfSteps) = sample.processModel()
    val startEnergy = startEnergies.first / EV_TO_DJ
    val endEnergy = endEnergies.first / EV_TO_DJ
    sample.saveState(outFolder = settings.outFolder, filename = settings.momentaFileName)

    val endTime = System.currentTimeMillis()

    val delimiter = "---------------------------------------------------"
    println("\n$delimiter")
    println("time of working is ${(endTime - startTime) / 1000.0} seconds")
    println("time of computation is ${(endTime - midTime) / 1000.0} seconds")
    println("sample size is ${settings.x}x${settings.y}x${settings.z} with ${settings.n} particles per ring")
    println("total number of particles is ${settings.x * settings.y * settings.z * settings.n}")
    println("")
    println("full energy on start is ${startEnergy.eFormat()} eV")
    println("energies on start is ${formatEnergies(startEnergies.second)} eV")
    println("full energy on end is ${endEnergy.eFormat()} eV")
    println("energies on end is ${formatEnergies(endEnergies.second)} eV")
    println("diff between enerfies is ${(startEnergy - endEnergy).eFormat()}")
    println("")
    println("number of simulation steps is $numberOfSteps")

//    if (endEnergy > startEnergy) {
//        println("\nWOOOOOOOOOOW\n")
//    }
    println("$delimiter\n")
}

fun hysteresisRun(settings: Settings) {

}