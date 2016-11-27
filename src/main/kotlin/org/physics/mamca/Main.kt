package org.physics.mamca

/**
 * Основная функция запуска симуляции
 * первым аргументом принимает путь к файлу с настройками
 * создает Sample и запускает моделирование
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException(
                "первым аргументом должен быть путь к файлу с настройками"
        )
    }
    val outFolder = args[1]
    val settings = loadSettingsFromJson(args[0])
    val sample = Sample(settings)
    sample.optimizeEnergy()
    sample.saveState(outFolder = outFolder)
}