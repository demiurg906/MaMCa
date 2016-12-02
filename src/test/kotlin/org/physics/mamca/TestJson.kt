package org.physics.mamca

import org.junit.Test
import org.physics.mamca.math.Vector
import kotlin.test.assertEquals

class TestJson {
    @Test
    fun testVector() {
        val vector = Vector(1.0, 1.0, 1.0)
        val json = vector.toJsonString()
        val newVector = Vector(json)
        assertEquals(vector, newVector)
    }

    @Test
    fun testParticle() {
        val particle = Particle(
                Vector(0.0, 0.0, 0.0),
                Vector(1.0, 1.0, 1.0),
                Vector(2.0, 2.0, 2.0),
                Sample()
        )
        val json = particle.toJsonString()
        val newParticle = Particle(json, Sample())
        assertEquals(particle, newParticle)
    }

    @Test
    fun testSample() {
        val resourcesFolder = "src/test/resources"
        val filename = "sample.json"
        val settings = getDefaultSettings()
        val newSettings = getDefaultSettings()
        newSettings.load = true
        newSettings.jsonPath = "$resourcesFolder/$filename"
        val sample = Sample(settings)
        sample.dumpToJsonFile(resourcesFolder, filename)
        val newSample = Sample(newSettings)
        assertEquals(sample, newSample)
    }

    @Test
    fun testSettings() {
        val settings = getDefaultSettings()
        val filename = "src/test/resources/settings.json"
        createSettingsJson(filename, settings)
        val newSettings = loadSettingsFromJson(filename)
        assertEquals(settings, newSettings)
    }
}