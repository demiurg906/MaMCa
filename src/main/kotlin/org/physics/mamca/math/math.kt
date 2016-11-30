package org.physics.mamca.math

import org.physics.mamca.util.equalsDouble

fun sqr(x: Double): Double = x * x

fun abs(vector: Vector): Double = vector.r

infix operator fun  Double.times(v: Vector): Vector {
    return v * this
}

fun norm(vector1: Vector, vector2: Vector): Vector = (vector1 % vector2).direction()

fun isKollinear(a: Vector, b: Vector): Boolean = equalsDouble(abs(a % b), 0.0)

enum class Axis {
    X, Y, Z
}