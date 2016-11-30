package org.physics.mamca.util

import org.physics.mamca.DELTA
import org.physics.mamca.DIGITS
import org.physics.mamca.PI2
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
fun Double.format(digits: Int = DIGITS): String = java.lang.String.format("%.${digits}f", this)