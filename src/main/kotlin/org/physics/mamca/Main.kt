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
    val settings = loadSettingsFromJson(settingsFile)
    val sample = Sample(settings)
    sample.dumpToJsonFile("$outFolder/sample.json")
    sample.optimizeEnergy()
    sample.saveState(outFolder = outFolder)
}