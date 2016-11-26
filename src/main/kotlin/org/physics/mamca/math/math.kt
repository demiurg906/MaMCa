package org.physics.mamca.math

fun sqr(x: Double): Double = x * x

fun abs(vector: Vector): Double = vector.r

infix operator fun  Double.times(v: Vector): Vector {
    return v * this
}

fun norm(vector1: Vector, vector2: Vector): Vector = (vector1 % vector2).direction()