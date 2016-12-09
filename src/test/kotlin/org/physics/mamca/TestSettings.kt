package org.physics.mamca

import org.junit.Test
import org.physics.mamca.util.deleteFile
import kotlin.test.assertEquals

class TestSettings {
    @Test
    fun exportImportWorks() {
        val jsonName = "testedSettings.json"
        val defaultSettings = Settings()
        createSettingsJson(jsonName, defaultSettings)
        val importedSettings = loadSettingsFromJson(jsonName)
        deleteFile(jsonName)
        assertEquals(defaultSettings, importedSettings)
    }
}