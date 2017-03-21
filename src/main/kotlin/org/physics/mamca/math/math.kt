package org.physics.mamca.math

import org.physics.mamca.util.equalsDouble

fun sqr(x: Double): Double = x * x

fun cub(x: Double): Double = x * x * x

fun log(a: Double, b: Double) = Math.log(a) / Math.log(b)

fun abs(vector: Vector): Double = vector.r

infix operator fun Double.times(v: Vector): Vector = v * this

infix operator fun Double.div(v: Vector): Vector = Vector(1.0 / v.x, 1.0 / v.y, 1.0 / v.z)

fun norm(vector1: Vector, vector2: Vector): Vector = (vector1 % vector2).direction()

fun isKollinear(a: Vector, b: Vector): Boolean = equalsDouble(abs(a % b), 0.0)

enum class Axis {
    X, Y, Z
}

fun rank(x: Int): Int {
    var res = 0
    var n = x
    while (n != 0) {
        n /= 10
        res += 1
    }
    if (res == 0)
        res = 1
    return res
}
