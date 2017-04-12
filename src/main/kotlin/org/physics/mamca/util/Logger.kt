package org.physics.mamca.util

object Logger {
    private val builder = StringBuilder()

    val DELIMITER = "---------------------------------------------------"

    fun info(s: String): Logger {
        _info(s + "\n")
        return this
    }

    private  fun _info(s: String) {
        print(s)
        builder.append(s)
    }

    fun addDelimiter(): Logger {
        _info(DELIMITER + "\r\n")
        return this
    }

    fun addLineBreak(): Logger {
        _info("\n")
        return this
    }

    override fun toString(): String = builder.toString()
}