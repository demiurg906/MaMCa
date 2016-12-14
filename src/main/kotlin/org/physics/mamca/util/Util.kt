package org.physics.mamca.util

import org.physics.mamca.*
import java.io.File
import java.lang.Math.acos
import java.nio.file.Files
import java.util.*


val random = Random()

fun randomTheta(): Double = acos(2 * random.nextDouble() - 1)
fun randomPhi(): Double = PI2 * random.nextDouble()

fun equalsDouble(one: Double, another: Double): Boolean {
    return Math.abs(one - another) < DELTA
}

fun <T>pairs(list: List<T>): List<Pair<T, T>> {
    /**
     * return список всевозможных различных пар из элементов списка list
     */
    val res: MutableList<Pair<T, T>> = mutableListOf()
    for (i in 0 until list.size) {
        for (j in (i + 1) until list.size) {
            res.add(Pair(list[i], list[j]))
        }
    }
    return res
}

fun deleteFile(path: String) = Files.delete(File(path).toPath())

/**
 * возвращает отформатированный Double
 * @param digits количество цифр после запятой
 */
fun Double.format(digits: Int = MATH_DIGITS): String = java.lang.String.format(Locale.US, "%.${digits}f", this)

/**
 * возвращает Double, отформатированный в e нотации
 * @param digits количество цифр после запятой
 */
fun Double.eFormat(digits: Int = DIGITS): String = java.lang.String.format(Locale.US, "%.${digits}e", this)

infix operator fun Triple<Double, Double, Double>.plus(it: Triple<Double, Double, Double>): Triple<Double, Double, Double> =
        Triple(this.first + it.first, this.second + it.second, this.third + it.third)

fun formatEnergies(energies: Triple<Double, Double, Double>, k: Double = 1 / EV_TO_DJ, digits: Int = DIGITS): String {
    val an = energies.first * k
    val int = energies.second * k
    val b = energies.third * k
    return "(an: ${an.eFormat(digits)}, int: ${int.eFormat(digits)}, B: ${b.eFormat(digits)})"
}
