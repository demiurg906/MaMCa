package org.physics.mamca

import org.physics.mamca.util.eFormat

/**
 * Основная функция запуска симуляции
 * первым аргументом принимает путь к файлу с настройками
 * создает Sample и запускает моделирование
 */
fun main(args: Array<String>) {
    val settingsFile: String
    val outFolder: String
    if (args.isEmpty()) {
        settingsFile = "./resources/settings.json"
        outFolder = "./resources/out"
    } else {
        settingsFile = args[0]
        outFolder = args[1]
    }
    val startTime = System.currentTimeMillis()

    val settings = loadSettingsFromJson(settingsFile)
    val sample = Sample(settings)
    sample.dumpToJsonFile(outFolder, "sample.json")
    sample.saveState(outFolder = outFolder, filename = "momenta_before.txt")

    val midTime = System.currentTimeMillis()
    var (startEnergy, endEnergy, numberOfSteps) = sample.processRelaxation()
    startEnergy /= EV_TO_DJ
    endEnergy /= EV_TO_DJ
    sample.saveState(outFolder = outFolder)

    val endTime = System.currentTimeMillis()

    val delimiter = "---------------------------------------------------"
    println("\n$delimiter")
    println("time of working is ${(endTime - startTime) / 1000.0} seconds")
    println("time of computation is ${(endTime - midTime) / 1000.0} seconds")
    println("sample size is ${settings.x}x${settings.y}x${settings.z} with ${settings.n} particles per ring")
    println("total number of particles is ${settings.x * settings.y * settings.z * settings.n}")
    println("number of \"two minimums\" situations is ${sample.twoMinimums}")
    println("energy on start is ${startEnergy.eFormat()} eV")
    println("energy on end is ${endEnergy.eFormat()} eV")
    println("number of simulation steps is $numberOfSteps")

    if (endEnergy > startEnergy) {
        println("\nWOOOOOOOOOOW\n")
    }
    println("$delimiter\n")
}

//fun tesWTF(settingsFile: String, outFolder: String) {
//    var settings = loadSettingsFromJson(settingsFile)
//    var sample = Sample(settings)
//    sample.dumpToJsonFile(outFolder, "sample.json")
//    var energy = sample.processRelaxation().first
//    energy /= EV_TO_DJ
//    fun printLog() {
//        val filename = "./resources/${settings.load}_sample"
//        File(filename).bufferedWriter().use { out ->
//            out.write("energy: $energy\n")
//            sample.particles.forEach { out.write("$it\n") }
//            out.write("b: ${sample.b}, momentaValue: ${sample.momentaValue}\n")
//            for (particle in sample.particles) {
//                for (dp in particle.dipolParticles) {
//                    out.write("$dp\n")
//                }
//                for (ep in particle.exchangeParticles) {
//                    out.write("$ep\n")
//                }
//            }
//        }
//        println(energy.eFormat(2))
//    }
//    printLog()
//    settings = loadSettingsFromJson(settingsFile)
//    settings.load = true
//    sample = Sample(settings)
//    energy = sample.processRelaxation().first
//    energy /= EV_TO_DJ
//    printLog()
//}
