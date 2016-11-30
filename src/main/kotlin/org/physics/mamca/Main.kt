package org.physics.mamca

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
    sample.dumpToJsonFile("$outFolder/sample.json")
    sample.optimizeEnergy()
    sample.saveState(outFolder = outFolder)

    val endTime = System.currentTimeMillis()
    val delimiter = "---------------------------------------------------"
    println("\n$delimiter")
    println("time of working is ${(endTime - startTime) / 1000.0} seconds")
    println("sample size is ${settings.x}x${settings.y}x${settings.z} with ${settings.n} particles per ring")
    println("total number of particles is ${settings.x * settings.y * settings.z * settings.n}")
    println("number of \"two minimums\" situations is ${sample.twoMinimums}")
    println("$delimiter\n")
}