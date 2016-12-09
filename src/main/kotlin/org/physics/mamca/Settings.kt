package org.physics.mamca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File


data class Settings(val x: Int = 1, // количество клеток по x
                    val y: Int = 1, // количество клеток по y
                    val z: Int = 1, // количество клеток по z
                    val n: Int = 1, // число частиц в кольце

                    val r: Double = 1.5, // радиус частицы
                    val d: Double = 20.0, // диаметр кольца [нм]
                    val offset: Double = 4.0, // расстояние между клетками [нм]

                    val m: Double = 800.0, // значение момента [магнетон бора, шт]
                    var kan: Double = 5.0e-2, // константа анизотропии [эВ]
                    var jex: Double = 1.0e2, // константа обмена [Тл^2 / эВ]

                    // расстояния, на которых чувтствуются взаимодействия:
                    val dipol_distance: Double = 30.0, // диполь-дипольное, [нм]
                    val exchange_distance: Double = 4.0, // обменное, [нм]

                    val viscosity: Double = 0.9, // коэффициент вязкости, 0 <= viscosity <= 1
                    val t: Double = 0.0, // температура [К]

                    val ot: Int = 0, // расположение осей анизотропии
                    // 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
                    // отклонение оси анизотропии от оси z и оси x соответственно
                    val ot_theta: Double = 90.0, // [градус]
                    val ot_phi: Double = 0.0, // [градус]

                    val b_x: Double = 0.0, // поле по x [Тл]
                    val b_y: Double = 0.0, // поле по y [Тл]
                    val b_z: Double = 0.0, // поле по z [Тл]

                    val precision: Int = 7, // точность (количтество шагов симуляции)
                    var load: Boolean = false, // загружать ли предыдущее состояние
                    var jsonPath: String = "./resources/out/sample.json" // путь к сохраненному состоянию
)

fun loadSettingsFromJson(filename: String): Settings {
    val mapper = jacksonObjectMapper()
    val settings: Settings = mapper.readValue(File(filename))
    return settings
}

fun createSettingsJson(filename: String, settings: Settings) {
    val mapper = jacksonObjectMapper()
    mapper.writeValue(File(filename), settings)
}

fun main(args: Array<String>) {
    // генерирует файл со стандартными настройками
    // путь к файлу передается в первом аргументе запуска
    // если запустить без аргументов, то используется путь ./defaultSettings.json
    val defaultSettings = Settings()
    val pathToDefaultSettings = if (args.isNotEmpty()) args[0] else "defaultSettings.json"
    createSettingsJson(pathToDefaultSettings, defaultSettings)
}