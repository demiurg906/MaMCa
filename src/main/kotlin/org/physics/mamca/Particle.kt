package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.physics.mamca.math.*
import org.physics.mamca.util.equalsDouble
import java.lang.Math.*
import java.lang.reflect.Type

class Particle {
    // положение частицы в пространстве
    val loc: Vector

    // значение магнитного момента
    var m: Vector

    // ось анизотропии
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

    /**
     * расчет эффективного магнитного поля
     */
    fun effectiveField(): Vector {
        var field = Vector()

        // фоновое магнитное поле
        field += sample.b

        // вклад диполь-дипольного взаимодействия
        var dipols = Vector()
        dipolParticles.forEach { dipols += it.m }
        field += dipols * sample.settings.ms

        // вклад обменного взаимодействия
        var exchange = Vector()
        exchangeParticles.forEach { exchange += it.m }
        field += exchange * sample.settings.jex

        return field
    }

    fun optimizeEnergy() {
        // эффективное поле
        val bEff = effectiveField()

        if (isKollinear(bEff, lma)) {
            // поле параллельно оси анизотропии
            TODO()
        }

        // нормаль к плоскости
        val eZ = norm(bEff, lma)

        // поворот момента в плоскость
        m -= eZ * (m * eZ)
        m.normalize()

        val theta = lma.angleTo(bEff, eZ)

        // значение энергии в зависимости от угла
        fun energy(phi: Double): Double =
                sample.settings.kan * sqr(sin(phi)) - abs(bEff) * sample.settings.m * cos(phi - theta)

        fun diffEnergy(phi: Double): Double =
                sample.settings.kan * 2 * sin(phi) * cos(phi) + abs(bEff) * sample.settings.m * sin(phi - theta)

        val a = 4 * sample.settings.kan
        val b = abs(bEff) * sample.settings.m * sin(theta)
        val c = 2 * abs(bEff) * sample.settings.m * cos(theta)

        // корни уравнения от phi
        val roots: List<Double>
        if (!equalsDouble(b, 0.0)) {
//            // уравнение четвертой степени
            val alpha = sqr(a) - 4 * sqr(b) - sqr(c)
            val beta = a * b * c
            val tau = (c - a) / b
            val delta = sqr(tau) / 2
            val epsilon = pow(sqrt(11664 * sqr(beta) - 108 * pow(alpha, 3.0)) + 108 * beta, 1.0 / 3.0) / (3 * pow(2.0, 1.0 / 3.0) * b)
            val mu = epsilon + alpha / epsilon
            val gamma = sqrt(mu + delta / 2)
            val nu = (pow(tau, 3.0) + 8 * (a + c) / b) / (4 * gamma)
            val psi = tau / 2
            val lambda = - mu + delta

            val nu1 = - gamma / 2 - psi
            val nu2 = gamma / 2 - psi
            val chi1 = sqrt(lambda + nu) / 2
            val chi2 = sqrt(lambda - nu) / 2

            // массив значений угла phi, в которых достигаются экстремумы энергии
            roots = listOf(nu1 - chi1, nu1 + chi1, nu2 - chi2, nu2 + chi2).map { 2 * atan(it) }.filter { it != Double.NaN }
            println(roots)
            val energies = roots.map(::energy)
            println(energies)
            val diffEnergies = roots.map(::diffEnergy)
            println(diffEnergies)


            fun diffWithX(x: Double): Double = b * pow(x, 4.0) + (c - a) * pow(x, 3.0) + (c + a) * x - b
            println(listOf(nu1 - chi1, nu1 + chi1, nu2 - chi2, nu2 + chi2).map(::diffWithX))
            println()
        } else {
            // уравнение третьей степени
            println("lol")
            roots = arrayListOf()
        }
        // минимумы
//        val mins = roots.filter{ it < 0}
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
