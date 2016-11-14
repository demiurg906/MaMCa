package org.physics.mamca

import org.junit.Test
import org.physics.mamca.Particle
import org.physics.mamca.Sample
import org.physics.mamca.math.Vector
import org.physics.mamca.getDefaultSettings
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
        val settings = getDefaultSettings()
        val newSettings = getDefaultSettings()
        newSettings.load = true
        newSettings.jsonPath = "src/test/resources/sample.json"
        val sample = Sample(settings)
        sample.dumpToJsonFile(newSettings.jsonPath)
        val newSample = Sample(newSettings)
        assertEquals(sample, newSample)
    }
}