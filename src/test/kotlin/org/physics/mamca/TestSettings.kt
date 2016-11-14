package org.physics.mamca

import org.junit.Test
import org.physics.mamca.createSettingsJson
import org.physics.mamca.util.deleteFile
import org.physics.mamca.getDefaultSettings
import org.physics.mamca.getSettingsFromJson
import org.physics.mamca.util.deleteFile
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals

class TestSettings {
    @Test
    fun exportImportWorks() {
        val jsonName = "testedSettings.json"
        val defaultSettings = getDefaultSettings()
        createSettingsJson(jsonName, defaultSettings)
        val importedSettings = getSettingsFromJson(jsonName)
        deleteFile(jsonName)
        assertEquals(defaultSettings, importedSettings)
    }
}