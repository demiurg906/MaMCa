package org.physics.mamca.util

import org.physics.mamca.Settings
import org.physics.mamca.booleanFields
import org.physics.mamca.stringFields
import java.io.File

/**
 * Скрипт, который генерирует питоновский аналог Settgins.kt
 * со стандартными параметрами
 */
fun main(args: Array<String>) {
    val code = StringBuilder()
    val tab = "    "
    val dTab = tab + tab
    val tTab = dTab + tab
    val fTab = tTab + tab

    // мапа с именами полей и их значениями по-умолчанию
    val fields = Settings().toString().substringAfter("Settings(").
            substringBeforeLast(")").split(", ").
            associate {
                val (key, value) = it.split("=")
                key to value
            }

    with (code) {
        append("\"\"\"\n")
        append("That file generated automatically, don't change it\n")
        append("\"\"\"\n")
        append("\n")

        append("import json\n")
        append("from collections import OrderedDict\n")
        append("\n\n")

        append("class Settings:\n")
        append("${tab}def __init__(self, filename=None):\n")
        append("${dTab}self._d = OrderedDict()\n")

        for ((field, value) in fields) {
            if (field in stringFields)
                append("${dTab}self._d['$field'] = '$value'\n")
            else if (field in booleanFields)
                append("${dTab}self._d['$field'] = ${value.capitalize()}\n")
            else
                append("${dTab}self._d['$field'] = $value\n")
        }

        append("${dTab}if filename is not None:\n")
        append("${tTab}with open(filename) as f:\n")
        append("${fTab}d = json.load(f)\n")
        append("${fTab}for key, value in d.items():\n")
        append("$fTab${tab}self._d[key] = value\n")
        append("\n")

        for (field in fields.keys) {
            append("$tab@property\n")
            append("${tab}def $field(self):\n")
            append("${dTab}return self._d['$field']\n")
            append("\n")
            append("$tab@$field.setter\n")
            append("${tab}def $field(self, value):\n")
            append("${dTab}self._d['$field'] = value\n")
            append("\n")
        }

        append("${tab}def __getitem__(self, key):\n")
        append("${dTab}return self._d[key]\n")
        append("\n")

        append("${tab}def __setitem__(self, key, value):\n")
        append("${dTab}self._d[key] = value\n")
        append("\n")

        append("${tab}def save_settings(self, filename):\n")
        append("${dTab}with open(filename, mode='w') as f:\n")
        append("${tTab}json.dump(self._d, f)\n")
        append("\n")

        // TODO: add pretty __str__
        append("${tab}def __str__(self):\n")
        append("${dTab}return ''\n")
        append("\n")
    }

    File("coordinator/mamca/settings.py").printWriter().use { out ->
        out.write(code.toString())
    }
}
