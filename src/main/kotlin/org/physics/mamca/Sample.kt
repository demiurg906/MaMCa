package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.FileUtils
import org.physics.mamca.math.Vector
import org.physics.mamca.math.abs
import org.physics.mamca.util.pairs
import org.physics.mamca.util.randomPhi
import org.physics.mamca.util.randomTheta
import java.io.File
import java.io.Serializable
import java.lang.Math.*
import java.lang.reflect.Type

class Sample : Serializable {
    val b: Vector
    val particles: MutableList<Particle>
    val settings: Settings

    var twoMinimums = 0

    /**
     * пустой конструктор-заглушка
     */
    constructor() {
        b = Vector(0.0, 0.0, 0.0)
        particles = mutableListOf()
        settings = getDefaultSettings()
    }

    /**
     * еще один конструктор-заглушка
     */
    constructor(b: Vector, particles: List<Particle>) {
        this.b = b
        this.particles = particles.toMutableList()
        this.settings = getDefaultSettings()
    }

    /**
     * основной конструктор, инициализирует образец на основе переданных настроек
     */
    constructor(settings: Settings) {
        this.settings = settings
        if (settings.load) {
            val json = FileUtils.readFileToString(File(settings.jsonPath))
            val gson = GsonBuilder().registerTypeAdapter(this.javaClass, SampleDeserializer()).create()
            val sample: Sample = gson.fromJson(json, getType())
            this.b = sample.b
            this.particles = sample.particles
        }
        else {
            val s = settings
            // магнитное поле
            b = Vector(s.b_x, s.b_y, s.b_z)

            // заполнение частицами
            particles = mutableListOf()

            // число частиц
            val numberOfParticles = s.x * s.y * s.z * s.n
            // угол между соседними частицами в кольце
            val alpha = PI2 / s.n
            // расстояние между центрами соседних колец
            val delta = s.d + s.offset
            // радиус кольца
            val r = s.d / 2
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
                        x * delta + r * cos(beta),
                        y * delta + r * sin(beta),
                        z * (1 + s.offset)
                )

                // начальное положение момента
                val m = Vector(s.m, randomTheta(), randomPhi(), polar = true)

                // ось анизотропии
                val lma: Vector
                when (s.ot) {
                    // случайно в 3D
                    0 -> lma = Vector(1.0, randomTheta(), randomPhi(), polar = true)
                    // случайно в 2D
                    1 -> lma = Vector(1.0, PI / 2, randomPhi(), polar = true)
                    // задано
                    2 -> lma = Vector(1.0, s.ot_theta, s.ot_phi, polar = true)
                    else -> throw IllegalArgumentException(
                            "ot in settings must be 0..2, but ${s.ot} given "
                    )
                }
                val p = Particle(loc, m, lma, this)
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
            if (dist < DIPOL_DIST) {
                dipolParticles[p1]?.add(p2)
                dipolParticles[p2]?.add(p1)
            }
            if (dist < EX_DIST) {
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
            if ((r * 2 > offset) and (x * y * z > 1)) {
                println("WARNNIG: rings overlap")
            }
        }
    }

    /**
     * основная функция, выполняющая релаксацию системы
     * релаксация происходит в несколько шагов, до тех пор, пока относительное изменение энергии, или пока
     * не будет сосчитано количество шагов, заданное в настройках
     * @return (энергия до оптимизации, энергия после оптимизации, количество шагов оптимизации)
     */
    fun processRelaxation(): Triple<Double, Double, Int> {
        var energies = optimizeEnergy()
        val startEnergy = energies.first

        val computeDelta: () -> Double = { abs(energies.first - energies.second) / energies.first}

        var relativeDeltaEnergy = computeDelta()
        var numberOfSteps = 1
        for (i in 1 until settings.precision) {
            if (relativeDeltaEnergy < RELATIVE_ENERGY_PRECISION) {
                break
            }
            energies = optimizeEnergy(energies.second)
            relativeDeltaEnergy = computeDelta()
            numberOfSteps += 1
        }

        val endEnergy = energies.second
        return Triple(startEnergy, endEnergy, numberOfSteps)
    }
    /**
     * @return суммарная энергию образца
     */
    fun computeEnergy(): Double {
        return particles.map { it.computeEnergy() }.sum()
    }

    /**
     * оптимизирует энергию всех частиц
     * @return энергию до оптимизации и после
     */
    private fun optimizeEnergy(computedOldEnergy: Double? = null): Pair<Double, Double> {
        val oldEnergy: Double
        if (computedOldEnergy == null) {
            particles.forEach { it.computeEffectiveField() }
            oldEnergy = computeEnergy()
        } else {
            oldEnergy = computedOldEnergy
        }
        particles.forEach { it.optimizeEnergy() }
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
                val x = p.loc.x
                val y = p.loc.y
                val z = p.loc.z

                val mx = p.m.x
                val my = p.m.y
                val mz = p.m.z

                val x1 = x - mx
                val x2 = x + mx
                val y1 = y - my
                val y2 = y + my
                val z1 = z - mz
                val z2 = z + mz
                out.write("$x1 $y1 $z1 $x2 $y2 $z2 $x $y $z\n")
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