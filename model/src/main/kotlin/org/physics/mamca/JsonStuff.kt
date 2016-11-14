package org.physics.mamca


import com.google.gson.*
import java.lang.reflect.Type

class ParticleSerializer : JsonSerializer<Particle> {
    override fun serialize(src: Particle?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        val loc = src!!.loc.toJsonString()
        jsonObject.addProperty("loc", loc)
        val m = src.m.toJsonString()
        jsonObject.addProperty("m", m)
        val lma = src.lma.toJsonString()
        jsonObject.addProperty("lma", lma)
        return jsonObject
    }
}

class ParticleDeserialiser : JsonDeserializer<Particle> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Particle {
        val jsonObject = json!!.asJsonObject!!

        val loc = Vector(jsonObject.get("loc").asString)
        val m = Vector(jsonObject.get("m").asString)
        val lma = Vector(jsonObject.get("lma").asString)
        val sample = Sample()

        val particle = Particle(loc, m, lma, sample)
        return particle
    }
}

class SampleSerializer : JsonSerializer<Sample> {
    override fun serialize(src: Sample?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        val b = src!!.b.toJsonString()
        jsonObject.addProperty("b", b)
        val particles = Gson().toJson(src.particles.map { it.toJsonString() })
        jsonObject.addProperty("particles", particles)
        return jsonObject
    }
}

class SampleDeserializer : JsonDeserializer<Sample> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Sample {
        val jsonObject = json!!.asJsonObject!!

        val b = Vector(jsonObject.get("b").asString)
        val emptyList: MutableList<String> = mutableListOf()
        val jsonParticles = Gson().fromJson(jsonObject.get("particles").asString,  emptyList.javaClass)
        val particles = jsonParticles.map { Particle(it) }

        val sample = Sample(b, particles)
        return sample
    }
}