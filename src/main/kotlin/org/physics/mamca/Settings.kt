package org.physics.mamca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

// при добавлении-удалении полей в класс настроек необходимо запустить скрипт
// `mamca.util.SettingsGenerator.kt` (генератор python кода) для обновления класса настроек в координаторе
data class Settings(val x: Int = 60, // количество клеток по x
                    val y: Int = 60, // количество клеток по y
                    val z: Int = 1, // количество клеток по z
                    val n: Int = 1, // число частиц в кольце

                    val r: Double = 1.25, // радиус частицы
                    val d: Double = 0.0, // диаметр кольца [нм]
                    val offset_x: Double = 4.0, // расстояние между клетками  по оси x [нм]
                    val offset_y: Double = 4.0, // расстояние между клетками  по оси y [нм]
                    val offset_z: Double = 4.0, // расстояние между клетками  по оси z [нм]

                    val m: Double = 800.0, // значение момента [магнетон бора, шт]
                    var kan: Double = 8000.0, // константа анизотропии [эВ/нм^3]
                    var jex: Double = 5.0e0, // константа обмена [Тл^2 / эВ]

                    // расстояния, на которых чувтствуются взаимодействия:
                    val dipolDistance: Double = 30.0, // диполь-дипольное, [нм]
                    val exchangeDistance: Double = 4.0, // обменное, [нм]

                    var viscosity: Double = 0.9, // коэффициент вязкости, 0 <= viscosity <= 1
                    var t: Double = 0.0, // температура [К]

                    val loc: Int = 0, // начальное расположение моментов
                    // 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
                    // отклонение оси анизотропии от оси z и оси x соответственно
                    val loc_theta: Double = 90.0, // [градус]
                    val loc_phi: Double = 0.0, // [градус]

                    val ot: Int = 0, // расположение осей анизотропии (аналогично loc)
                    val ot_theta: Double = 90.0, // [градус]
                    val ot_phi: Double = 0.0, // [градус]

                    var b_x: Double = 0.0, // поле по x [Гаусс]
                    var b_y: Double = 0.0, // поле по y [Гаусс]
                    var b_z: Double = 0.0, // поле по z [Гаусс]

                    var time: Double = 1.0, // время релаксации [с]
                    val timeStep: Int = 100, // временной шаг [нс]

                    val cyclicBoundaries: Boolean = false, // использовать ли граничные цикличиские условия по x и y

                    val name: String = "default", // имя модели (используется для графиков и логов)

                    val precision: Int = 1000, // точность (количество шагов симуляции)
                    var load: Boolean = false, // загружать ли предыдущее состояние
                    var jsonPath: String = "./resources/data/default/out/sample.json", // путь к сохраненному состоянию

                    val hysteresis: Boolean = false, // нужно ли запускать в режиме гистерезиса
                    val hysteresisSteps: Int = 7, // количество шагов гистерезиса в ветке от нуля до края
                    val hysteresisDenseSteps: Int = 2, // количество больших шагов в частой области
                    val hysteresisDenseMultiplier: Int = 2, // отношение шага в обычной области к шагу в частой области

                    val is2dPlot: Boolean = true, // рисовать трехмерные графики или двумерные
                    val xAxis: String = "x", // координата по оси абсциис (2D график)
                    val yAxis: String = "y", // координата по оси ординат (2D график)

                    val borders: Boolean = true, // отсекать ли часть клеток при рисовании графиков
                    val leftX: Int = 20,  //       ...|.......|...
                    val rightX: Int = 40, //        leftX   rightX
                    val leftY: Int = 20,  // с Y координатой аналогично
                    val rightY: Int = 40, //

                    val dataFolder: String = "../data", // путь к папке для выходных данных

                    val isParallel: Boolean = false, // использовать ли параллельные вычисления
                    val memory: Int = 6144 // количество памяти, выделяемой для java-машины, Мбайт
)

// списки с полями типа string и boolean
// костыль
val stringFields = setOf("name", "jsonPath", "dataFolder", "outFolder", "picFolder", "logFolder", "xAxis", "yAxis")
val booleanFields = setOf("load", "hysteresis", "is2dPlot", "isParallel", "borders", "cyclicBoundaries")

// количество полей в блоке, отделенном от остальных новой строкой
// нужен, чтобы поля были логически разделены пустыми строками
val newLines = listOf(4, 5, 3, 2, 2, 3, 3, 3, 2, 1, 1, 3, 4, 3, 5, 1, 3)

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