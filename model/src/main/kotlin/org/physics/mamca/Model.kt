package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.PrintWriter
import java.io.Serializable
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.reflect.Type


class Particle {
    val loc: Vector
    val m: Vector
    val lma: Vector
    val sample: Sample

    private var neighborsInitialized = false
    private var dipolParticles: Set<Particle> = setOf()
    private var exchangeParticles: Set<Particle> = setOf()

    constructor(loc: Vector, m: Vector, lma: Vector, sample: Sample) {
        this.loc = loc
        this.m = m
        this.lma = lma
        this.sample = sample
    }

    constructor(json: String, sample: Sample = Sample()) {
        // десериализует частицу из json строки
        // массивы соседей в json не входят

        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, ParticleDeserialiser()).create()
        val particle: Particle = gson.fromJson(json, getType())
        this.loc = particle.loc
        this.m = particle.m
        this.lma = particle.lma
        this.sample = sample
    }

    fun initializeNeighbors(dipolParticles: Set<Particle>,
                            exchangeParticles: Set<Particle>) {
        if (neighborsInitialized) {
            throw IllegalStateException("neighbors can be initialized only once")
        }
        this.dipolParticles = dipolParticles
        this.exchangeParticles = exchangeParticles
        neighborsInitialized = true
    }


    fun effectiveField(): Vector {
        TODO()
    }

    fun optimizeEnergy() {
        TODO()
    }

    fun computeEnergy(): Double {
        TODO()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Particle

        if (loc != other.loc) return false
        if (m != other.m) return false
        if (lma != other.lma) return false
//        if (sample != other.sample) return false
//        if (neighborsInitialized != other.neighborsInitialized) return false
//        if (dipolParticles != other.dipolParticles) return false
//        if (exchangeParticles != other.exchangeParticles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loc.hashCode()
        result = 31 * result + m.hashCode()
        result = 31 * result + lma.hashCode()
//        result = 31 * result + sample.hashCode()
//        result = 31 * result + neighborsInitialized.hashCode()
//        result = 31 * result + dipolParticles.hashCode()
//        result = 31 * result + exchangeParticles.hashCode()
        return result
    }

    fun toJsonString(): String {
        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, ParticleSerializer()).setPrettyPrinting().create()
        val json = gson.toJson(this, getType())
        return json
    }

    fun getType(): Type {
        return TypeToken.get(this.javaClass).type
    }

}

class Sample : Serializable {
    val b: Vector
    val particles: MutableList<Particle>
    val settings: Settings

    constructor() {
        // пустой конструктор-заглушка

        b = Vector(0.0, 0.0, 0.0)
        particles = mutableListOf()
        settings = getDefaultSettings()
    }

    constructor(b: Vector, particles: List<Particle>) {
        this.b = b
        this.particles = particles.toMutableList()
        this.settings = getDefaultSettings()
    }

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
    }

    fun computeEnergy(): Double {
        TODO()
    }

    fun saveState(outFolder: String = "", filename: String = "momenta.txt") {
        TODO()
    }

    fun toJsonString(): String {
        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, SampleSerializer()).setPrettyPrinting().create()
        val json = gson.toJson(this, getType())
        return json
    }

    fun dumpToJsonFile(path: String) {
        val printWriter = PrintWriter(path)
        val json = toJsonString()
        printWriter.write(json)
        printWriter.flush()
        printWriter.close()
    }

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