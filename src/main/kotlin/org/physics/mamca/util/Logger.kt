package org.physics.mamca.util

object Logger {
    private val builder = StringBuilder()

    val DELIMETER = "---------------------------------------------------"

    fun info(s: String): Logger {
        builder.append(s).append("\n")
        return this
    }

    fun addDelimiter(): Logger {
        builder.append(DELIMETER).append("\n")
        return this
    }

    fun addLineBreak(): Logger {
        builder.append("\n")
        return this
    }

    override fun toString(): String = builder.toString()
}