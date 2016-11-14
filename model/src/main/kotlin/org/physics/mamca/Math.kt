package org.physics.mamca

import com.google.gson.Gson

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

    constructor(x: Double, y: Double, z: Double, polar: Boolean = false) {
        // конструирует вектор по координатам (декартовым или полярным)

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

    constructor(json: String) {
        // десериализует вектор из json строки

        val gson = Gson()
        val vector: Vector = gson.fromJson(json, this.javaClass)
        this.x = vector.x
        this.y = vector.y
        this.z = vector.z
        this.r = vector.r
        this.theta = vector.theta
        this.phi = vector.phi
    }

    private fun updateDecardCoordinates() {
        val sinTheta = Math.sin(theta)
        x = r * sinTheta * Math.cos(phi)
        y = r * sinTheta * Math.sin(phi)
        z = r * Math.cos(theta)
    }

    private fun updatePolarCoordinates() {
        r = Math.sqrt(x * x + y * y + z * z)
        theta = Math.atan2(Math.sqrt(x * x + y * y), z)
        phi = Math.atan2(y, x)
    }

    fun normalize() {
        r = 1.0
        updateDecardCoordinates()
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

    operator fun times(other: Vector): Double {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    operator fun times(c: Double): Vector {
        return Vector(x * c, y * c, z * c)
    }

    operator fun div(c: Double): Vector {
        return Vector(x / c, y / c, z / c)
    }

    operator fun mod(other: Vector): Vector {
        return Vector(this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x)
    }

    operator fun plusAssign(other: Vector) {
        x += other.x
        y += other.y
        z += other.z
        updatePolarCoordinates()
    }

    operator fun minusAssign(other: Vector) {
        x -= other.x
        y -= other.y
        z -= other.z
        updatePolarCoordinates()
    }

    operator fun timesAssign(c: Double) {
        x *= c
        y *= c
        z *= c
        updatePolarCoordinates()
    }

    operator fun divAssign(c: Double) {
        x /= c
        y /= c
        z /= c
        updatePolarCoordinates()
    }

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
        return "Vector(x=$x, y=$y, z=$z)"
    }

    fun toJsonString(): String {
        val gson = Gson()
        val json = gson.toJson(this)
        return json
    }


}

fun abs(vector: Vector): Double = vector.r

operator fun Double.times(v: Vector): Vector {
    return v * this
}