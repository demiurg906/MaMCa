package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.physics.mamca.math.*
import org.physics.mamca.util.Mathematica
import org.physics.mamca.util.format
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
        dipolParticles.forEach {
            val r = it.loc - loc
            val n = r.direction()
            dipols += (3 *(n * it.m) * n - it.m) / pow(abs(r), 3.0)
        }
        bEff += dipols * sample.momentaValue * DIPOL_CONST

        // вклад обменного взаимодействия
        var exchange = Vector()
        exchangeParticles.forEach { exchange += it.m }
        bEff += exchange * sample.settings.jex
    }

    fun optimizeEnergy() {
        optimizeMomentaPosition()
        thermalFluctuations()
    }

    /**
     * оптимизирует энергию, путем скатывания момента к положению минимума энергии
     * (с учетом вязкости)
     */
    fun optimizeMomentaPosition() {
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
        val b = abs(bEff) * sample.momentaValue * sin(theta)
        val c = 2 * abs(bEff) * sample.momentaValue * cos(theta)

        // уравнение четвертой степени
        val expr = "${b.format()} x^4 + ${(c-a).format()} x^3 + ${(c+a).format()} x - ${b.format()} == 0"

        // корни уравнения (с переходом от x к phi)
        val roots = Mathematica.findRoots(expr).map { 2 * atan(it) }

        // энергия в экстремумах
        val energies = roots.map { computeEnergyInPlane(it, theta)}

        val energyPerPhi = (energies zip roots).sortedBy { it.first }

        // минимумы (пары (энергия, угол))
        val mins = energyPerPhi.dropLast(energyPerPhi.size / 2)

        // максимумы (пары (энергия, угол))
        val maxs = energyPerPhi.drop(energyPerPhi.size / 2)

        // итоговое значение phi, на которое будет повернут момент после поворота
        val phi: Double
        if (mins.size == 1) {
            phi = mins[0].second
        } else {
            // два минимума, падаем в ближайший
            sample.twoMinimums += 1

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
        rotateMomentaToAngle(phi, eZ)
    }

    /**
     * описывает тепловые колебания момента
     */
    fun thermalFluctuations() {
        // TODO: реализовать
    }

    /**
     * поворачивает момент к положению минимума
     * @param minPhi положение минимума
     * @param eZ нормаль к плоскости, в которой все происходит
     */
    fun rotateMomentaToAngle(minPhi: Double, eZ: Vector) {
        val currentPhi = lma.angleTo(m, eZ)
        val deltaPhi = (currentPhi - minPhi) * sample.settings.viscosity
        m = Matrix(eZ, deltaPhi) * m
    }

    /**
     * расчитывает энергию момента
     */
    fun computeEnergy(): Double = sample.settings.kan * sqr(abs(m % lma)) - (m * bEff)

    /**
     * расчитывает энергию момента, отклоненного от оси анизотропии на угол phi
     * @param phi угол между осью анизотропии и моментом
     * @param theta угол между осью анизотропии и эффективным полем
     */
    fun computeEnergyInPlane(phi: Double, theta: Double): Double =
            sample.settings.kan * sqr(sin(phi)) - abs(bEff) * sample.momentaValue * cos(phi - theta)

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
