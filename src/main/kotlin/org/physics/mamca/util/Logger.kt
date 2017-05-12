package org.physics.mamca.util

object Logger {
    private val builder = StringBuilder()

    val DELIMITER = "---------------------------------------------------"
    val LINE_SEPARATOR = System.lineSeparator()!!

    fun info(s: String): Logger {
        _info(s + LINE_SEPARATOR)
        return this
    }

    private  fun _info(s: String) {
        print(s)
        builder.append(s)
    }

    fun addDelimiter(): Logger {
        _info(DELIMITER + LINE_SEPARATOR)
        return this
    }

    fun addLineBreak(): Logger {
        _info(LINE_SEPARATOR)
        return this
    }

    override fun toString(): String = builder.toString()
}