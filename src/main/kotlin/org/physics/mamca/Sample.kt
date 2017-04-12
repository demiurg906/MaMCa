package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.FileUtils
import org.physics.mamca.math.Vector
import org.physics.mamca.math.abs
import org.physics.mamca.util.*
import sun.rmi.runtime.Log
import java.io.File
import java.io.Serializable
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.*

class Sample : Serializable {
    var b: Vector
    val particles: MutableList<Particle>
    val settings: Settings
    val momentaValue: Double

    // частицы, у которых обнаружилось два минимума
    var twoMinimums: MutableSet<Particle> = mutableSetOf()

    val KT: Double
    val vKan: Double // константа анизотропиии на объем частицы
    val random = Random()

    var nJumps = 0

    /**
     * пустой конструктор-заглушка
     */
    constructor() {
        b = Vector(0.0, 0.0, 0.0)
        particles = mutableListOf()
        settings = Settings()
        this.momentaValue = 0.0
        this.KT = 1.0
        this.vKan = 1.0
    }

    /**
     * еще один конструктор-заглушка
     */
    constructor(particles: List<Particle>) {
        this.b = Vector()
        this.particles = particles.toMutableList()
        this.settings = Settings()
        this.momentaValue = 0.0
        this.KT = 1.0
        this.vKan = 1.0
    }

    /**
     * основной конструктор, инициализирует образец на основе переданных настроек
     */
    constructor(settings: Settings) {
        this.settings = settings

        // перевод констант в единицы с Дж
        this.momentaValue = settings.m * MU_B

        this.KT = settings.t * K
        this.vKan = settings.kan * 4 * PI * Math.pow(settings.r, 3.0)

        b = Vector(settings.b_x, settings.b_y, settings.b_z)

        // инициализация образца
        if (settings.load) {
            val json = FileUtils.readFileToString(File(settings.jsonPath), Charset.defaultCharset())
            val gson = GsonBuilder().registerTypeAdapter(this.javaClass, SampleDeserializer()).create()
            val sample: Sample = gson.fromJson(json, getType())
            this.particles = sample.particles
            this.particles.forEach { it.sample = this }
        }
        else {
            val s = settings

            // заполнение частицами
            particles = mutableListOf()

            // число частиц
            val numberOfParticles = s.x * s.y * s.z * s.n
            // угол между соседними частицами в кольце
            val alpha = PI2 / s.n
            // расстояние между центрами соседних колец
            val delta_x = s.d + s.offset_x
            val delta_y = s.d + s.offset_y
            // радиус кольца
            val r = s.d / 2

            // отклонение направления моментов в радианах
            val loc_theta = Math.toRadians(s.loc_theta)
            val loc_phi = Math.toRadians(s.loc_phi)

            // отклонение оси анизотропии в радианах
            val ot_theta = Math.toRadians(s.ot_theta)
            val ot_phi = Math.toRadians(s.ot_phi)

            for (i in 0 until numberOfParticles) {
                // параметры для определения координат частицы
                val cell: Int = i / s.n
                val n: Int = i % s.n
                val z: Int = cell % s.z
                val lvl: Int = cell / s.z
                val x: Int = lvl / s.y
                val y: Int = lvl % s.y

                // полный угол частицы в кольце
                val beta = n * alpha

                // координаты частицы
                val loc = Vector(
                        x * delta_x + r * cos(beta),
                        y * delta_y + r * sin(beta),
                        z * (1 + s.offset_z)
                )

                // начальное положение момента
                val m: Vector
                when (s.loc) {
                // случайно в 3D
                    0 -> m = Vector(1.0, randomTheta(), randomPhi(), polar = true)
                // случайно в 2D
                    1 -> m = Vector(1.0, PI / 2, randomPhi(), polar = true)
                // задано
                    2 -> m = Vector(1.0, loc_theta, loc_phi, polar = true)
                    else -> throw IllegalArgumentException(
                            "ot in settings must be 0..2, but ${s.ot} given "
                    )
                }

                // ось анизотропии
                val lma: Vector
                when (s.ot) {
                    // случайно в 3D
                    0 -> lma = Vector(1.0, randomTheta(), randomPhi(), polar = true)
                    // случайно в 2D
                    1 -> lma = Vector(1.0, PI / 2, randomPhi(), polar = true)
                    // задано
                    2 -> lma = Vector(1.0, ot_theta, ot_phi, polar = true)
                    else -> throw IllegalArgumentException(
                            "ot in settings must be 0..2, but ${s.ot} given "
                    )
                }
                val p = Particle(loc, m, lma, Triple(x, y, z), this)
                particles.add(p)
            }

        }
        // определение соседей частиц
        val dipolParticles: Map<Particle, MutableList<Particle>> =
                particles.associate { Pair(it, mutableListOf<Particle>()) }
        val exchangeParticles: Map<Particle, MutableList<Particle>> =
                particles.associate { Pair(it, mutableListOf<Particle>()) }
        for ((p1, p2) in pairs(particles)) {
            // расстояние между частицами
            val dist = abs(p1.loc - p2.loc)
            if (dist < settings.dipolDistance) {
                dipolParticles[p1]?.add(p2)
                dipolParticles[p2]?.add(p1)
            }
            if (dist < settings.exchangeDistance) {
                exchangeParticles[p1]?.add(p2)
                exchangeParticles[p2]?.add(p1)
            }
        }
        for (p in particles) {
            p.initializeNeighbors(dipolParticles[p]!!.toSet(), exchangeParticles[p]!!.toSet())
        }

        with(settings) {
            // проверка, что все частицы помещаются на кольце
            if (n * r * 2 > d * PI) {
                println("WARNING: boundaries of ring particles overlap")
            }

            // проверка, что кольца на досточном расстоянии друг от друга
            listOf(offset_x, offset_y, offset_z).forEachIndexed { i, offset ->
                val axis: String
                when (i) {
                    0 -> axis = "x"
                    1 -> axis = "y"
                    2 -> axis = "z"
                    else -> axis = "ERROR"
                }
                if ((r * 2 > offset) and (x * y * z > 1)) {
                    println("WARNING: rings overlap on axis $axis")
                }
            }

        }
    }

    /**
     * основная функция, оптимизируящая энергию
     * @return (энергия до оптимизации, энергия после оптимизации, количество шагов оптимизации)
     */
    fun processModel(outFolder: String? = null): Triple<
            Pair<Double, Triple<Double, Double, Double>>,
            Pair<Double, Triple<Double, Double, Double>>,
            Int> {
        if ((outFolder == null) and !settings.hysteresis) {
            throw IllegalStateException("outFolder can be null only in hysteresis run")
        }

        /**
         * функция, сохраняющая состояние образца после релаксации после прыжка для
         * негистерезисного запуска (для наблюдения релаксации системы)
         */
        fun saveStateAfterJump(t: Int, exactlyAfter: Boolean = false) {
            val id = if (exactlyAfter) 1 else 2

            if (!settings.hysteresis) {
                saveState(outFolder!!, "momenta_${nJumps.format()}_${id}_${(t / S_TO_NS).format(9)}.txt")
            }
        }

        var res = processRelaxation()
        saveStateAfterJump(0)
        val startEnergy = res.first

        if (settings.t > 0) {
            Logger.addDelimiter()
//            Logger.info("times of jumps [s]:\n")
            for (t in 0..settings.time.toInt() step settings.timeStep) {
                if (twoMinimums.isEmpty()) {
                    Logger.info("oops, no minimums, t = ${(t / S_TO_NS).format(9)} s, number of jumps is $nJumps")
                    break
                }
                if (energyJumps()) {
//                    Logger.info((t / S_TO_NS).format(9))
                    nJumps += 1
                    saveStateAfterJump(t, true)
                    res = processRelaxation()
                    saveStateAfterJump(t, false)
                }
            }
        }
        return Triple(startEnergy, res.second, res.third)
    }

    fun energyJumps(): Boolean {
        var res = false
        for (particle in twoMinimums) {
            res = particle.energyJump() or res
        }
        return res
    }

    /**
     * основная функция, выполняющая релаксацию системы
     * релаксация происходит в несколько шагов, до тех пор, пока относительное изменение энергии, или пока
     * не будет сосчитано количество шагов, заданное в настройках
     * @return (энергия до оптимизации, энергия после оптимизации, количество шагов оптимизации)
     */
    fun processRelaxation(): Triple<
            Pair<Double, Triple<Double, Double, Double>>,
            Pair<Double, Triple<Double, Double, Double>>,
            Int> {
        if (settings.isParallel) {
            particles.parallelStream().forEach { it.computeEffectiveField() }
        } else {
            particles.forEach { it.computeEffectiveField() }
        }
        // energies on start
        val startEnergy = computeEnergies()

        var energies = optimizeEnergy()

        val computeDelta: () -> Double = {
            val delta = (energies.first - energies.second) / energies.first
            if (delta < 0)
                1 - delta
            else
                delta
        }

        var relativeDeltaEnergy = computeDelta()
        var numberOfSteps = 1
        var endedWithPrecision = true
        for (i in 1 until settings.precision) {
            if (relativeDeltaEnergy < RELATIVE_ENERGY_PRECISION) {
                endedWithPrecision = false
                break
            }
            energies = optimizeEnergy(energies.second)
            relativeDeltaEnergy = computeDelta()
            numberOfSteps += 1
        }

        if (endedWithPrecision) {
            Logger.info("Processing ended with precision. Relative delta energy is $relativeDeltaEnergy")
        }

        // energies on end
        val endEnergy = computeEnergies()
        return Triple(startEnergy, endEnergy, numberOfSteps)
    }

    /**
     * @return суммарная энергию образца (пара из полной энергии и тройки энергий по взаимодействиям)
     */
    fun computeEnergies(): Pair<Double, Triple<Double, Double, Double>> {
        var partE = Triple(0.0, 0.0, 0.0)
        particles.map { it.computeEnergies() }.forEach { partE += it }
        val fullE = partE.first + partE.second + partE.third
        return fullE to partE
    }

    /**
     * @return суммарная энергия образца
     */
    fun computeEnergy(): Double = particles.map { it.computeEnergy() }.sum()


    /**
     * оптимизирует энергию всех частиц
     * @return энергии до оптимизации и после
     */
    private fun optimizeEnergy(computedOldEnergy: Double? = null): Pair<Double, Double> {
        twoMinimums = mutableSetOf()
        val oldEnergy: Double
        if (computedOldEnergy == null) {
            oldEnergy = computeEnergy()
        } else {
            oldEnergy = computedOldEnergy
        }
        if (settings.isParallel) {
            particles.parallelStream().forEach { it.optimizeEnergy() }
        } else {
            particles.forEach { it.optimizeEnergy() }
        }
        particles.forEach { it.computeEffectiveField() }
        val newEnergy = computeEnergy()
        return oldEnergy to newEnergy
    }

    /**
     * сохраняет состояние всех моментов
     * @param outFolder путь к папке, в которую надо сохранять
     * @param filename имя сохраняемого файла
     */
    fun saveState(outFolder: String = ".", filename: String = "momenta.txt") {
        // записывает в файл текущее состояние моментов
        val path = outFolder + File.separator + filename
        File(path).printWriter().use { out ->
            for (p in particles) {
                // координтаты, [нм]
                val x = p.loc.x
                val y = p.loc.y
                val z = p.loc.z

                // момент, безразмерный, нормированный
                val mx = p.m.x / 2
                val my = p.m.y / 2
                val mz = p.m.z / 2

                val x1 = x - mx
                val x2 = x + mx
                val y1 = y - my
                val y2 = y + my
                val z1 = z - mz
                val z2 = z + mz

                val cellX = p.cell.first
                val cellY = p.cell.second
                val cellZ = p.cell.third
                out.write("$x1 $y1 $z1 $x2 $y2 $z2 $x $y $z $cellX $cellY $cellZ\n")
            }
        }
    }

    /**
     * сериализует образец в json строку
     */
    fun toJsonString(): String {
        // возвращает сериализованную json строку
        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, SampleSerializer()).setPrettyPrinting().create()
        val json = gson.toJson(this, getType())
        return json
    }

    /**
     * сериализует образец в json файл
     * @param outFolder путь к папке, в которую надо сохранять
     * @param filename имя сохраняемого файла
     */
    fun dumpToJsonFile(outFolder: String, filename: String) {
        File("$outFolder/$filename").printWriter().use { out ->
            val json = toJsonString()
            out.write(json)
        }
    }

    /**
     * возвращает java-класс образца (служебная функция)
     */
    fun getType(): Type {
        return TypeToken.get(this.javaClass).type
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Sample

//        if (settings != other.settings) return false
        if (b != other.b) return false
        if (particles != other.particles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = settings.hashCode()
        result = 31 * result + b.hashCode()
        result = 31 * result + particles.hashCode()
        return result
    }
}
