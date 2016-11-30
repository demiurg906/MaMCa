package org.physics.mamca.math

import org.junit.Test
import org.physics.mamca.PI
import org.physics.mamca.PI2
import org.physics.mamca.math.Axis.*
import kotlin.test.assertEquals

class TestMatrix {
    @Test
    fun testMatrixTimesVector() {
        val vector = Vector(1.0, 2.0, 3.0)
        val matrix = Matrix(listOf(
                listOf(4.0, 5.0, 6.0),
                listOf(7.0, 8.0, 9.0),
                listOf(10.0, 11.0, 12.0)
        ))
        val res = matrix * vector
        val expected = Vector(32, 50, 68)
        assertEquals(expected, res)
    }

    @Test
    fun testRotateXMatrix() {
        _testRotateMatrix(X, Vector(1, 0, 0))
    }

    @Test
    fun testRotateYMatrix() {
        _testRotateMatrix(Y, Vector(0, 1, 0))
    }

    @Test
    fun testRotateZMatrix() {
        _testRotateMatrix(Z, Vector(0, 0, 1))
    }

    fun _testRotateMatrix(axis: Axis, norm: Vector) {
        for (theta in thetas()) {
            val expected = Matrix(axis, theta)
            val matrix = Matrix(norm, theta)
            assertEquals(expected, matrix)
        }
    }

    fun thetas(): List<Double> = listOf(0.0, 1.0, PI / 2, PI, PI2, PI / 4)
}
