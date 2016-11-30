package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.physics.mamca.math.*
import org.physics.mamca.util.Mathematica
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

    private var bEff: Vector = Vector()

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
    fun computeEffectiveField() {
        bEff = Vector()

        // фоновое магнитное поле
        bEff += sample.b

        // вклад диполь-дипольного взаимодействия
        var dipols = Vector()
        dipolParticles.forEach { dipols += it.m }
        bEff += dipols * sample.settings.ms

        // вклад обменного взаимодействия
        var exchange = Vector()
        exchangeParticles.forEach { exchange += it.m }
        bEff += exchange * sample.settings.jex
    }

    fun optimizeEnergy() {
        // эффективное поле
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

        val a = 4 * sample.settings.kan
        val b = abs(bEff) * sample.settings.m * sin(theta)
        val c = 2 * abs(bEff) * sample.settings.m * cos(theta)

        // корни уравнения от phi
        val roots: List<Double>
        if (!equalsDouble(b, 0.0)) {
            // уравнение четвертой степени
            /* решаем уравнение ручками
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

             массив значений угла phi, в которых достигаются экстремумы энергии
            roots = listOf(nu1 - chi1, nu1 + chi1, nu2 - chi2, nu2 + chi2).map { 2 * atan(it) }
                    .map {if (it == Double.NaN) Double.POSITIVE_INFINITY else it}*/
            val expr = "$b x^4 + ${c-a} x^3 + ${c+a} x - $b == 0"
            val xRoots = Mathematica.findRoots(expr)
//            println("x roots: $xRoots")

            fun diffWithX(x: Double): Double = b * pow(x, 4.0) + (c - a) * pow(x, 3.0) + (c + a) * x - b
            xRoots.map(::diffWithX).map { assert(equalsDouble(it, 0.0)) }
//            println("expr': ${xRoots.map(::diffWithX)}")

            roots = xRoots.map { 2 * atan(it) }
//            val energies = roots.map { computeEnergy(it, theta)}
//            println("energies: $energies")

            //значение производной энергии
            fun diffEnergy(phi: Double): Double =
                sample.settings.kan * 2 * sin(phi) * cos(phi) + abs(bEff) * sample.settings.m * sin(phi - theta)
            val diffEnergies = roots.map(::diffEnergy)
            diffEnergies.map { assert(equalsDouble(it, 0.0)) }
//            println("diffEnergies: $diffEnergies")

//            println()
        } else {
            // уравнение третьей степени
            val x = sqrt((a + c) / (a - c))
            roots = arrayListOf(0.0, x, -x).filter { it != Double.NaN }.map { 2 * atan(it) }
        }
        // энергия в экстремумах
        val energies = roots.map { computeEnergy(it, theta)}

        // минимумы (пары (энергия, угол))
        val mins = (energies zip roots).filter { it.first < 0 }
        // максимумы (пары (энергия, угол))
        val maxs = (energies zip roots).filter { it.first > 0 }
        if (mins.size == 1) {
            rotateMomenta(mins[0].second, eZ)
            // проверка, что повернулось хорошо
            val currentPhi = lma.angleTo(m , eZ)
//            println("phi: ${mins[0].second.format()}, ${currentPhi.format()}")
//            println("energy: ${mins[0].first.format()}, ${computeEnergy(currentPhi, theta).format()}")
        } else if (mins.size > 1) {
            // TODO: нужно определять, в какой мминимум падать
            TODO()
        } else {
            // TODO: ну а вдруг?
            TODO()
        }
    }

    /**
     * поворачивает момент к положению минимума
     * @param minPhi положение минимума
     * @param eZ нормаль к плоскости, в которой все происходит
     */
    fun rotateMomenta(minPhi: Double, eZ: Vector) {
        val currentPhi = lma.angleTo(m, eZ)
        val deltaPhi = (currentPhi - minPhi) * sample.settings.viscosity
        m = Matrix(eZ, deltaPhi) * m
    }

    fun computeEnergy(): Double {
        val eZ = norm(bEff, lma)
        val phi = lma.angleTo(m , eZ)
        val theta = lma.angleTo(bEff, eZ)
        return computeEnergy(phi, theta)
    }

    /**
     * расчитывает энергию момента, отклоненного от оси анизотропии на угол phi
     * @param phi угол между осью анизотропии и моментом
     * @param theta угол между осью анизотропии и эффективным полем
     */
    fun computeEnergy(phi: Double, theta: Double): Double =
            sample.settings.kan * sqr(sin(phi)) - abs(bEff) * sample.settings.m * cos(phi - theta)

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
