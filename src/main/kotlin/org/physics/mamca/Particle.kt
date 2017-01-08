package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.physics.mamca.math.*
import org.physics.mamca.util.Mathematica
import org.physics.mamca.util.eFormat
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

    // нормаль к плоскости, в которой вращается момент
    var eZ: Vector = Vector()

    var sample: Sample

    var maxs: List<Pair<Double, Double>> = listOf()
    var mins: List<Pair<Double, Double>> = listOf()

    var energy: Double = 0.0

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
        dipolParticles.forEach {
            val r = it.loc - loc
            val n = r.direction()
            dipols += (3 *(n * it.m) * n - it.m) / pow(abs(r), 3.0)
        }
        bEff += dipols * sample.momentaValue * DIPOL_CONST

        // вклад обменного взаимодействия
        var exchange = Vector()
        exchangeParticles.forEach { exchange += it.m }
        bEff += exchange * sample.settings.jex * sample.momentaValue
    }

    /**
     * оптимизирует энергию, путем скатывания момента к положению минимума энергии
     * (с учетом вязкости)
     */
    fun optimizeEnergy() {
        val theta: Double

        if (isKollinear(bEff, lma)) {
            // поле параллельно оси анизотропии
            if (isKollinear(lma, m)) {
                TODO()
            }
            eZ = norm(m, lma)
            if (lma * bEff < 0) {
                theta = PI
            } else {
                theta = 0.0
            }
        } else {
            // поле не параллельно оси анизотропии
            eZ = norm(bEff, lma)
            theta = lma.angleTo(bEff, eZ)

            // поворот момента в плоскость поля и анизотропии
            m -= eZ * (m * eZ)
            m.normalize()
        }

        val a = 4 * sample.settings.kan
        val b = abs(bEff) * sample.momentaValue * sin(theta)
        val c = 2 * abs(bEff) * sample.momentaValue * cos(theta)

        // уравнение четвертой степени
        val expr = "${b.eFormat(MATH_DIGITS)} x^4 + ${(c-a).eFormat(MATH_DIGITS)} x^3 + ${(c+a).eFormat(MATH_DIGITS)} x - ${b.eFormat(MATH_DIGITS)} == 0"

        // корни уравнения (с переходом от x к phi)
        val roots = Mathematica.findRoots(expr).map { 2 * atan(it) }

        // энергия в экстремумах
        val energies = roots.map { computeEnergyInPlane(it, theta)}

        val energyPerPhi = (energies zip roots).sortedBy { it.first }

        // минимумы (пары (энергия, угол))
        mins = energyPerPhi.dropLast(energyPerPhi.size / 2)

        // максимумы (пары (энергия, угол))
        maxs = energyPerPhi.drop(energyPerPhi.size / 2)

        // итоговое значение phi, на которое будет повернут момент после поворота
        val phi: Double
        if (mins.size == 1) {
            phi = mins[0].second
        } else {
            // два минимума, падаем в ближайший
            sample.twoMinimums.add(this)

            // все относительные углы от 0 до 2 Pi
            // текущее положение момента
            val currentPhi = lma.angleTo(m, eZ)
            var deltaPhi = mins[0].second - currentPhi
            if (deltaPhi < 0) {
                deltaPhi += PI2
            }
            // углы от текущего до максимумов
            val phiMaxes = maxs.map { it.second - currentPhi }.map { if (it < 0) it + PI2 else it }

            // считаем, сколько максимумов лежит между текущим углом и минимумов
            // если четное число -- то в этот минимум скатиться можно
            if (phiMaxes.count { deltaPhi - it > 0 } % 2 == 0) {
                phi = mins[0].second
            } else {
                phi = mins[1].second
            }
        }
        rotateMomentaToAngle(phi)
        energy = computeEnergy()
    }

    /**
     * описывает тепловые колебания момента
     */
    fun energyJump(): Boolean {
        val deltaE = Math.min(maxs[0].first, maxs[1].first) - energy
        val p = Math.exp(-deltaE / sample.KT)
//        println(p.eFormat())
        if (sample.random.nextDouble() < p) {
            val currentPhi = lma.angleTo(m, eZ)
            val minPhi = if (equalsDouble(currentPhi, mins[0].second)) mins[1].second else mins[0].second
            rotateMomentaToAngle(minPhi)
            return true
        }
        return false
    }

    /**
     * поворачивает момент к положению минимума
     * @param minPhi положение минимума
     */
    fun rotateMomentaToAngle(minPhi: Double) {
        val currentPhi = lma.angleTo(m, eZ)
        val deltaPhi = (currentPhi - minPhi) * sample.settings.viscosity
        m = Matrix(eZ, deltaPhi) * m
    }

    /**
     * расчитывает энергию момента
     */
    fun computeEnergy(): Double = sample.settings.kan * sqr(abs(m % lma)) + sample.momentaValue * ((m * bEff) - (abs(m) * abs(bEff)))

    /**
     * расчитывает энергии анизотропии, внешнего поля и взаимодействий
     * @return (eAnisotropy, eInteraction, eField)
     */
    fun computeEnergies(): Triple<Double, Double, Double>{
        val eAn = sample.settings.kan * sqr(abs(m % lma))
        val eBeff = (- (m * bEff) + (abs(m) * abs(bEff))) * sample.momentaValue
        val eB = (- m * sample.b + (abs(m) * abs(sample.b))) * sample.momentaValue
        return Triple(eAn, eBeff - eB, eB)
    }

    /**
     * расчитывает энергию момента, отклоненного от оси анизотропии на угол phi
     * @param phi угол между осью анизотропии и моментом
     * @param theta угол между осью анизотропии и эффективным полем
     */
    fun computeEnergyInPlane(phi: Double, theta: Double): Double =
            sample.settings.kan * sqr(sin(phi)) - abs(bEff) * sample.momentaValue * (cos(phi - theta) - 1)

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

    override fun toString(): String = "loc :${this.loc}, lma: ${this.lma}, m: ${this.m}"

    fun toJsonString(): String {
        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, ParticleSerializer()).setPrettyPrinting().create()
        val json = gson.toJson(this, getType())
        return json
    }

    fun getType(): Type {
        return TypeToken.get(this.javaClass).type
    }

}
