package org.physics.mamca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File


data class Settings(val x: Int = 1, // количество клеток по x
                    val y: Int = 1, // количество клеток по y
                    val z: Int = 1, // количество клеток по z
                    val n: Int = 50, // число частиц в кольце

                    val r: Double = 1.5, // радиус частицы
                    val d: Double = 60.0, // диаметр кольца [нм]
                    val offset: Double = 4.0, // расстояние между клетками [нм]

                    val m: Double = 800.0, // значение момента [магнетон бора, шт]
                    var kan: Double = 5.0e-2, // константа анизотропии [эВ]
                    var jex: Double = 1.0e2, // константа обмена [Тл^2 / эВ]

                    // расстояния, на которых чувтствуются взаимодействия:
                    val dipolDistance: Double = 30.0, // диполь-дипольное, [нм]
                    val exchangeDistance: Double = 4.0, // обменное, [нм]

                    val viscosity: Double = 0.9, // коэффициент вязкости, 0 <= viscosity <= 1
                    val t: Double = 0.0, // температура [К]

                    val ot: Int = 2, // расположение осей анизотропии
                    // 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
                    // отклонение оси анизотропии от оси z и оси x соответственно
                    val ot_theta: Double = 90.0, // [градус]
                    val ot_phi: Double = 0.0, // [градус]

                    val b_x: Double = 0.0, // поле по x [Тл]
                    val b_y: Double = 0.0, // поле по y [Тл]
                    val b_z: Double = 0.0, // поле по z [Тл]

                    var time: Double = 1.0, // время релаксации [с]

                    val precision: Int = 7, // точность (количтество шагов симуляции)
                    var load: Boolean = false, // загружать ли предыдущее состояние
                    var jsonPath: String = "./resources/out/sample.json", // путь к сохраненному состоянию

                    val hysteresis: Boolean = false, // нужно ли запускать в режиме гистерезиса
                    val hysteresisSteps: Int = 7, // количество шагов гистерезиса в ветке от нуля до края

                    val outFolder: String = "./resources/out", // путь к папке для выходных данных
                    val momentaFileName: String = "momenta.txt" // имя файла, для итогового состояния
)

// списки с полями типа string и boolean
// костыль
val stringFields = setOf("jsonPath", "outFolder", "momentaFileName")
val booleanFields = setOf("load", "hysteresis")

// количество полей в блоке, отделенном от остальных новой строкой
// нужен, чтобы поля были логически разделены пустыми строками
val newLines = listOf(4, 3, 3, 2, 2, 3, 3, 1, 3, 2, 3)

fun loadSettingsFromJson(filename: String): Settings {
    val mapper = jacksonObjectMapper()
    val settings: Settings = mapper.readValue(File(filename))
    return settings
}

fun createSettingsJson(filename: String, settings: Settings) {
    val fields = settings.toString().substringAfter("Settings(").
            substringBeforeLast(")").split(", ").
            associate {
                val (key, value) = it.split("=")
                key to value
            }
    val code = StringBuilder()
    with (code) {
        append("{\n")
        var i = 0
        var linesCounter = 0
        for ((field, value) in fields) {
            if (field in stringFields) {
                append("  \"$field\":\"$value\",\n")
            } else {
                append("  \"$field\":$value,\n")
            }
            i += 1
            if (i == newLines[linesCounter]) {
                append("\n")
                linesCounter += 1
                i = 0
            }
        }
        deleteCharAt(lastIndexOf(","))
        append("}\n")
    }

    File(filename).printWriter().use { out ->
        out.write(code.toString())
    }
}

fun main(args: Array<String>) {
    // генерирует файл со стандартными настройками
    // путь к файлу передается в первом аргументе запуска
    // если запустить без аргументов, то используется путь ./defaultSettings.json
    val defaultSettings = Settings()
    val pathToDefaultSettings = if (args.isNotEmpty()) args[0] else "./resources/defaultSettings.json"
    createSettingsJson(pathToDefaultSettings, defaultSettings)
}