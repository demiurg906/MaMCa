package org.physics.mamca

import org.junit.Test
import kotlin.test.assertEquals

class TestVector {
    @Test
    fun testConstructDecardCoordinates() {
        val testDecardVectors = {x: Double, y: Double, z: Double,
                                 r: Double, theta: Double, phi: Double ->
            val vector = Vector(x, y, z)
            assertDoubleEquals(vector.r, r)
            assertDoubleEquals(vector.theta, theta)
            assertDoubleEquals(vector.phi, phi)
        }

        testDataWithFun(constructorsData(), testDecardVectors)
    }

    @Test
    fun testConstructPolarCoordinates() {
        val testPolarVectors = {x: Double, y: Double, z: Double,
                                r: Double, theta: Double, phi: Double ->
            val vector = Vector(r, theta, phi, polar = true)
            assertDoubleEquals(vector.x, x)
            assertDoubleEquals(vector.y, y)
            assertDoubleEquals(vector.z, z)
        }

        testDataWithFun(constructorsData(), testPolarVectors)
    }

    @Test
    fun testDifferentConstructors() {
        val testTwoVectors = {x: Double, y: Double, z: Double,
                              r: Double, theta: Double, phi: Double ->
            val vector1 = Vector(x, y, z)
            val vector2 = Vector(r, theta, phi, polar = true)
            assertEquals(vector1, vector2)
        }
        testDataWithFun(constructorsData(), testTwoVectors)
    }

    @Test
    fun testNormalize() {
        val testNormTwoVectors = {x: Double, y: Double, z: Double,
                              r: Double, theta: Double, phi: Double ->
            val vector1 = Vector(x, y, z)
            vector1.normalize()
            val vector2 = Vector(r, theta, phi, polar = true)
            assertEquals(vector1, vector2)
        }

        val data = arrayOf(
                arrayOf(2.0, 0.0, 0.0, 1.0, PI / 2, 0.0),
                arrayOf(0.0, 4.1, 0.0, 1.0, PI / 2, PI / 2),
                arrayOf(0.0, 0.0, 5.9, 1.0, 0.0, 0.0)
        )

        testDataWithFun(data, testNormTwoVectors)
    }

    @Test
    fun testUnaryPlus() {
        val testUnaryPlus = { x: Double, y: Double, z: Double ->
            val vector1 = Vector(x, y, z)
            val vector2 = +vector1
            assertEquals(vector1, vector2)
        }

        testDataWithFun(oneVectorData(), testUnaryPlus)
    }

    @Test
    fun testUnaryMinus() {
        val testUnaryMinus = {x: Double, y: Double, z: Double ->
            val vector = -Vector(x, y, z)
            assertDoubleEquals(-x, vector.x)
            assertDoubleEquals(-y, vector.y)
            assertDoubleEquals(-z, vector.z)
        }

        testDataWithFun(oneVectorData(), testUnaryMinus)
    }
    
    @Test 
    fun testPlus() {
        val testPlus = {x1: Double, y1: Double, z1: Double,
                        x2: Double, y2: Double, z2: Double ->
            val vector = Vector(x1, y1, z1) + Vector(x2, y2, z2)
            assertDoubleEquals(x1 + x2, vector.x)
            assertDoubleEquals(y1 + y2, vector.y)
            assertDoubleEquals(z1 + z2, vector.z)
        }

        testDataWithFun(twoVectorsData(), testPlus)
    }

    @Test
    fun testMinus() {
        val testMinus = {x1: Double, y1: Double, z1: Double,
                         x2: Double, y2: Double, z2: Double ->
            val vector = Vector(x1, y1, z1) - Vector(x2, y2, z2)
            assertDoubleEquals(x1 - x2, vector.x)
            assertDoubleEquals(y1 - y2, vector.y)
            assertDoubleEquals(z1 - z2, vector.z)
        }

        testDataWithFun(twoVectorsData(), testMinus)
    }

    @Test
    fun testTimesVectors() {
        val testTimes = {x1: Double, y1: Double, z1: Double,
                         x2: Double, y2: Double, z2: Double,
                         expected: Double ->
            val res = Vector(x1, y1, z1) * Vector(x2, y2, z2)
            assertDoubleEquals(res, expected)
        }

        testDataWithFun(twoVectorsWithTimesData(), testTimes)
    }

    @Test
    fun testTimesDouble() {
        val testTimes = {x: Double, y: Double, z: Double, c: Double ->
            fun test(vector: Vector) {
                assertDoubleEquals(x * c, vector.x)
                assertDoubleEquals(y * c, vector.y)
                assertDoubleEquals(z * c, vector.z)
            }

            test(Vector(x, y, z) * c)
            test(c * Vector(x, y, z))
        }

        testDataWithFun(oneVectorWithTimesData(), testTimes)
    }

    @Test
    fun testCrossVectors() {
        val testCross = {x1: Double, y1: Double, z1: Double,
                         x2: Double, y2: Double, z2: Double,
                         x3: Double, y3: Double, z3: Double ->
            val vector = Vector(x1, y1, z1) % Vector(x2, y2, z2)
            val expected = Vector(x3, y3, z3)
            assertEquals(expected, vector)
        }

        testDataWithFun(twoVectorsWithCrossData(), testCross)
    }
}



fun assertDoubleEquals(expected: Double, actual: Double) = assert(Math.abs(expected - actual) < DELTA)

fun constructorsData(): Array<Array<Double>> = arrayOf(
        arrayOf(1.0, 0.0, 0.0, 1.0, PI / 2, 0.0),
        arrayOf(0.0, 1.0, 0.0, 1.0, PI / 2, PI / 2),
        arrayOf(0.0, 0.0, 1.0, 1.0, 0.0, 0.0)
)

fun oneVectorData(): Array<Array<Double>> = arrayOf(
        arrayOf(2.0, 0.0, 0.0),
        arrayOf(0.0, 4.1, 0.0),
        arrayOf(0.0, 0.0, 5.9)
)

fun oneVectorWithTimesData(): Array<Array<Double>> = arrayOf(
        arrayOf(2.0, 0.0, 0.0, 2.0),
        arrayOf(0.0, 4.1, 0.0, 5.0),
        arrayOf(9.0, 4.0, 5.9, 0.0),
        arrayOf(0.0, 0.0, 0.0, 4.0)
)

fun twoVectorsData(): Array<Array<Double>> = arrayOf(
        arrayOf(1.0, 0.0, 0.0, 1.0, PI / 2, 0.0),
        arrayOf(0.0, 1.0, 0.0, 1.0, PI / 2, PI / 2),
        arrayOf(0.0, 0.0, 1.0, 1.0, 0.0, 0.0),
        arrayOf(0.0, 0.0, 0.0, 1.0, 0.0, 0.0)
)

fun twoVectorsWithCrossData(): Array<Array<Double>> = arrayOf(
        arrayOf(1.0, 0.0, 0.0, 3.0, 0.0, 1.0, 0.0, -1.0, 0.0),
        arrayOf(1.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        arrayOf(1.0, 1.0, 1.0, 0.0, 4.0, -3.0, -7.0, 3.0, 4.0)
)

fun twoVectorsWithTimesData(): Array<Array<Double>> = arrayOf(
        arrayOf(1.0, 2.0, 8.0, 1.0, 1.0, 1.0, 11.0),
        arrayOf(0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0),
        arrayOf(0.0, 1.0, 0.0, 1.0, 3.0, 0.0, 3.0)
)

fun testDataWithFun(data: Array<Array<Double>>, testFun: (Double, Double, Double,
                                                          Double, Double, Double,
                                                          Double, Double, Double) -> Unit) {
    data.forEach { testFun(it[0], it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8]) }
}


fun testDataWithFun(data: Array<Array<Double>>, testFun: (Double, Double, Double, Double, Double, Double, Double) -> Unit) {
    data.forEach { testFun(it[0], it[1], it[2], it[3], it[4], it[5], it[6]) }
}

fun testDataWithFun(data: Array<Array<Double>>, testFun: (Double, Double, Double, Double, Double, Double) -> Unit) {
    data.forEach { testFun(it[0], it[1], it[2], it[3], it[4], it[5]) }
}

fun testDataWithFun(data: Array<Array<Double>>, testFun: (Double, Double, Double, Double) -> Unit) {
    data.forEach { testFun(it[0], it[1], it[2], it[3]) }
}

fun testDataWithFun(data: Array<Array<Double>>, testFun: (Double, Double, Double) -> Unit) {
    data.forEach { testFun(it[0], it[1], it[2]) }
}