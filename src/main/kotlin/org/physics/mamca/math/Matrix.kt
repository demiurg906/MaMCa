package org.physics.mamca.math

import org.physics.mamca.math.Axis.*
import org.physics.mamca.util.equalsDouble

class Matrix {
    val m: List<List<Double>>

    /**
     * конструирует нулевую матрицу
     */
    constructor() {
        m = listOf(
                listOf(0.0, 0.0, 0.0),
                listOf(0.0, 0.0, 0.0),
                listOf(0.0, 0.0, 0.0)
        )
    }

    /**
     * конструирует матрицу с заданными значениями
     */
    constructor(m: List<List<Double>>) {
        this.m = m
    }

    /**
     * конструирует матрицу поворота вокруг оси axis на угол theta
     */
    constructor(axis: Vector, theta: Double) {
        val v: Vector
        if (!equalsDouble(abs(axis), 1.0))
            v = axis.direction()
        else
            v = axis
        val x = v.x
        val y = v.y
        val z = v.z
        val cos = Math.cos(theta)
        val minusCos = 1 - cos
        val sin = Math.sin(theta)
        m = listOf(
                listOf(cos + minusCos * sqr(x), minusCos * x * y - sin * z, minusCos * x * z + sin * y),
                listOf(minusCos * y * z + sin * z, cos + minusCos * sqr(y), minusCos * y * z - sin * x),
                listOf(minusCos * z * x - sin * y, minusCos * z * y + sin * x, cos + minusCos * sqr(z))
        )
    }

    /**
     *  конструирует матрицу поворота вокруг одной из базисных осей
     */
    constructor(axis: Axis, theta: Double) {
        val cos = Math.cos(theta)
        val sin = Math.sin(theta)
        when (axis) {
            X -> m = listOf(
                    listOf(1.0, 0.0, 0.0),
                    listOf(0.0, cos, -sin),
                    listOf(0.0, sin, cos)
                )
            Y -> m = listOf(
                    listOf(cos, 0.0, sin),
                    listOf(0.0, 1.0, 0.0),
                    listOf(-sin, 0.0, cos)
                )
            Z -> m = listOf(
                    listOf(cos, -sin, 0.0),
                    listOf(sin, cos, 0.0),
                    listOf(0.0, 0.0, 1.0)
                )
        }
    }

    /**
     * перемножение матрицы на вектор
     */
    operator fun times(vector: Vector): Vector {
        val res: MutableList<Double> = mutableListOf()
        val v = vector.asList()
        (0 until 3).mapTo(res) { i -> (0 until 3).sumByDouble { j -> v[j] * m[i][j] } }
        return Vector(res)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Matrix

        for (i in 0..2)
            for (j in 0..2)
                if (!equalsDouble(this.m[i][j], other.m[i][j]))
                    return false

        return true
    }

    override fun hashCode(): Int {
        return m.hashCode()
    }

    override fun toString(): String {
        var res = "("
        for (i in 0..2) {
            res += "(${m[i][0]}, ${m[i][1]}, ${m[i][2]})"
            if (i != 2)
                res += "\n"
        }
        res += ")"
        return res
    }


}