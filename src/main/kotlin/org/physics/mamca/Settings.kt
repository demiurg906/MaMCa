package org.physics.mamca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.Serializable


data class Settings(val x: Int, // количество клеток по x
                    val y: Int, // количество клеток по y
                    val z: Int, // количество клеток по z
                    val n: Int, // число частиц в кольце

                    val r: Double, // радиус частицы
                    val d: Double, // диаметр кольца [нм]
                    val offset: Double, // расстояние между клетками [нм]

                    val m: Double, // значение момента [ядерный магнетон, шт]
                    val kan: Double, // константа анизотропии [иДж], основной порядок -- 10^2 - 10^3
                    val jex: Double, // константа обмена [Тл^2 / иДж], основной порядок -- 10^-2 - 10^-3

                    // расстояния, на которых чувтствуются взаимодействия:
                    val dipol_distance: Double, // диполь-дипольное, [нм]
                    val exchange_distance: Double, // обменное, [нм]

                    val viscosity: Double, // коэффициент вязкости, 0 <= viscosity <= 1
                    val t: Double, // температура [К]

                    val ot: Int, // расположение осей анизотропии
                    // 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
                    // отклонение оси анизотропии от оси z и оси x соответственно
                    val ot_theta: Double, // [градус]
                    val ot_phi: Double, // [градус]

                    val b_x: Double, // поле по x [Тл]
                    val b_y: Double, // поле по y [Тл]
                    val b_z: Double, // поле по z [Тл]

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
                1, 1, 1, 10, // размеры
                1.0, 20.0, 4.0, // параметры колец
                1.0, 1.0e-2, 1.0e2, // константы взаимодействий
                30.0, 4.0, // расстояния взаимодействий
                1.0, 0.1, // вязкость и температура
                0, PI / 2, 0.0, // анизотропия
                0.0, 0.0, 0.0, // поле
                7, false, "sample.json" // точность
        )

fun main(args: Array<String>) {
    // генерирует файл со стандартными настройками
    // путь к файлу передается в первом аргументе запуска
    // если запустить без аргументов, то используется путь ./defaultSettings.json
    val defaultSettings = getDefaultSettings()
    val pathToDefaultSettings = if (args.isNotEmpty()) args[0] else "defaultSettings.json"
    createSettingsJson(pathToDefaultSettings, defaultSettings)
}