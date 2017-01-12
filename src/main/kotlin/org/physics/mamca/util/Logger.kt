package org.physics.mamca.util

object Logger {
    private val builder = StringBuilder()

    val DELIMITER = "---------------------------------------------------"

    fun info(s: String): Logger {
        _info(s + "\n")
        return this
    }

    private  fun _info(s: String) {
        println(s)
        builder.append(s)
    }

    fun addDelimiter(): Logger {
        _info(DELIMITER + "\n")
        return this
    }

    fun addLineBreak(): Logger {
        _info("\n")
        return this
    }

    override fun toString(): String = builder.toString()
}