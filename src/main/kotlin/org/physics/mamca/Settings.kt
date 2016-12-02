package org.physics.mamca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.Serializable


data class Settings(val x: Int, // количество клеток по x
                    val y: Int, // количество клеток по y
                    val z: Int, // количество клеток по z
                    val r: Double, // радиус частицы

                    val n: Int, // число частиц в кольце
                    val d: Double, // диаметр кольца [нм]
                    val offset: Double, // расстояние между клетками [нм]

                    val m: Double, // значение момента [ядерный магнетон, шт]
                    val kan: Double, // константа анизотропии [Дж]
                    val jex: Double, // константа обмена [ГТл / Дж]

                    val viscosity: Double, // коэффициент вязкости, 0 <= viscosity <= 1
                    val t: Double, // температура [К]

                    val ot: Int, // расположение осей анизотропии
                    // 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
                    // отклонение оси анизотропии от оси z и оси x соответственно
                    val ot_theta: Double,
                    val ot_phi: Double,

                    val b_x: Double, // поле по x [ГТл]
                    val b_y: Double, // поле по y [ГТл]
                    val b_z: Double, // поле по z [ГТл]

                    val precision: Int, // точность (количтество шагов симуляции)
                    var load: Boolean, // загружать ли предыдущее состояние
                    var jsonPath: String
) : Serializable

fun loadSettingsFromJson(filename: String): Settings {
    val mapper = jacksonObjectMapper()
    val settings: Settings = mapper.readValue(File(filename))
    return settings
}

fun createSettingsJson(filename: String, settings: Settings) {
    val mapper = jacksonObjectMapper()
    mapper.writeValue(File(filename), settings)
}

fun getDefaultSettings(): Settings =
        Settings(
            4, 4, 1, 1.0,
            4, 10.0, 0.0,
            8e4, 0.1, 1.0,
            1.0, 0.1,
            0, PI / 2, 0.0,
            0.0, 0.0, 0.0,
            7, false, "sample.json"
        )

fun main(args: Array<String>) {
    // генерирует файл со стандартными настройками
    // путь к файлу передается в первом аргументе запуска
    // если запустить без аргументов, то используется путь ./defaultSettings.json
    val defaultSettings = getDefaultSettings()
    val pathToDefaultSettings = if (args.isNotEmpty()) args[0] else "defaultSettings.json"
    createSettingsJson(pathToDefaultSettings, defaultSettings)
}