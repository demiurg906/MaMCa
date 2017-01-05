package org.physics.mamca.math

import com.google.gson.Gson
import org.physics.mamca.util.eFormat
import org.physics.mamca.util.equalsDouble

class Vector {
    var x: Double = 0.0
        private set
    var y: Double = 0.0
        private set
    var z: Double = 0.0
        private set
    var r: Double = 0.0
        private set
    var theta: Double = 0.0
        private set
    var phi: Double = 0.0
        private set


    /**
     * конструирует вектор по координатам (декартовым или полярным)
     */
    /*
    да, по хорошему надо из этого конструктора вызывать конструктор со списком,
    но это ухудшает быстродействие
     */
    constructor(x: Double, y: Double, z: Double, polar: Boolean = false) {
        if (polar) {
            this.r = x
            this.theta = y
            this.phi = z
            updateDecardCoordinates()
        } else {
            this.x = x
            this.y = y
            this.z = z
            updatePolarCoordinates()
        }
    }

    constructor(x: Int, y: Int, z: Int, polar: Boolean = false): this(x.toDouble(), y.toDouble(), z.toDouble(), polar)

    /**
     * конструирует нулевой вектор
     */
    constructor(): this(0.0, 0.0, 0.0, false)

    /**
     * конструироет копию вектора
     */
    constructor(other: Vector): this(other.x, other.y, other.z, false)

    /**
     * конструирует вектор по списку координат
     */
    constructor(coords: List<Double>, polar: Boolean = false): this(coords[0], coords[1], coords[2], polar) {
        if (coords.size != 3)
            throw IllegalArgumentException("coords must contains only 3 coordinates")
        if (polar) {
            this.r = coords[0]
            this.theta = coords[1]
            this.phi = coords[2]
            updateDecardCoordinates()
        } else {
            this.x = coords[0]
            this.y = coords[1]
            this.z = coords[2]
            updatePolarCoordinates()
        }
    }

    /**
     * десериализует вектор из json строки
     */
    constructor(json: String) {
        val gson = Gson()
        val vector: Vector = gson.fromJson(json, this.javaClass)
        this.x = vector.x
        this.y = vector.y
        this.z = vector.z
        this.r = vector.r
        this.theta = vector.theta
        this.phi = vector.phi
    }

    /**
     * пересчет декартовых координат
     */
    private fun updateDecardCoordinates() {
        val sinTheta = Math.sin(theta)
        x = r * sinTheta * Math.cos(phi)
        y = r * sinTheta * Math.sin(phi)
        z = r * Math.cos(theta)
    }


    /**
     * пересчет полярных координат
     */
    private fun updatePolarCoordinates() {
        r = Math.sqrt(x * x + y * y + z * z)
        theta = Math.atan2(Math.sqrt(x * x + y * y), z)
        phi = Math.atan2(y, x)
    }

    /**
     * нормировка на единицу
     */
    fun normalize() {
        r = 1.0
        updateDecardCoordinates()
    }

    /**
     * вовращает вектор, нормированный на единицу
     */
    fun direction(): Vector {
        val res = Vector(this)
        res.normalize()
        return res
    }

    operator fun unaryPlus(): Vector {
        return Vector(x, y, z)
    }

    operator fun unaryMinus(): Vector {
        return Vector(-x, -y, -z)
    }

    operator fun plus(other: Vector): Vector {
        return Vector(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    operator fun minus(other: Vector): Vector {
        return Vector(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    /**
     * скалярное произведение векторов
     */
    operator fun times(other: Vector): Double {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    operator fun times(c: Double): Vector {
        return Vector(x * c, y * c, z * c)
    }

    operator fun div(c: Double): Vector {
        return Vector(x / c, y / c, z / c)
    }

    /**
     * векторное произведение веторов
     */
    operator fun mod(other: Vector): Vector {
        return Vector(this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x)
    }

    /**
     * возвращает направленное значение угла между двумя векторами
     */
    fun angleTo(other: Vector, eZ: Vector): Double {
        val eX = other.direction()
        val eY = (eZ % eX).direction()
        val thisTan = this * eX
        val thisNorm = this * eY
        return Math.atan2(thisNorm, thisTan)
    }

    /**
     * возвращает ненаправленное значение угла между двумя векторами
     */
    fun angleTo(other: Vector): Double {
        return Math.acos(this * other)
    }

    /**
     * возвращает декартовы координаты в виде списка
     */
    fun asList(): List<Double> = listOf(x, y, z)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Vector

        if (!equalsDouble(x, other.x)) return false
        if (!equalsDouble(y, other.y)) return false
        if (!equalsDouble(z, other.z)) return false
        if (!equalsDouble(r, other.r)) return false
        if (!equalsDouble(phi, other.phi)) return false
        if (!equalsDouble(theta, other.theta)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + r.hashCode()
        result = 31 * result + theta.hashCode()
        result = 31 * result + phi.hashCode()
        return result
    }

    override fun toString(): String {
        return "(${x.eFormat(2)}, ${y.eFormat(2)}, ${z.eFormat(2)})"
    }

    /**
     * сериализация вектора в json строку
     */
    fun toJsonString(): String {
        val gson = Gson()
        val json = gson.toJson(this)
        return json
    }
}