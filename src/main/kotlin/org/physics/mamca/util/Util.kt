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

fun formatEnergies(energies: List<Double>, k: Double = 1 / EV_TO_DJ, digits: Int = DIGITS): String {
    val dipol = energies[1] * k
    val exchange = energies[2] * k
    val b = energies[0] * k
    val an = energies[3] * k
    return "(an: ${an.eFormat(digits)}, " +
            "dipol: ${dipol.eFormat(digits)}, " +
            "ex: ${exchange.eFormat(digits)}, " +
            "B: ${b.eFormat(digits)})"
}

fun Int.format(digits: Int = DIGITS): String = java.lang.String.format(Locale.US, "%0${digits}d", this)

fun playSuccessNotification() {
    try {
        Sound.playSound("./resources/notifications/arpeggio.wav").join()
    } catch (e: Exception) {
        println("playing notification failed")
    }
}