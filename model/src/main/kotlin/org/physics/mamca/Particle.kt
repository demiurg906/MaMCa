package org.physics.mamca

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.physics.mamca.math.Vector
import java.lang.reflect.Type

class Particle {
    val loc: Vector
    val m: Vector
    val lma: Vector
    val sample: Sample

    private var neighborsInitialized = false
    private var dipolParticles: Set<Particle> = setOf()
    private var exchangeParticles: Set<Particle> = setOf()

    constructor(loc: Vector, m: Vector, lma: Vector, sample: Sample) {
        this.loc = loc
        this.m = m
        this.lma = lma
        this.sample = sample
    }

    constructor(json: String, sample: Sample = Sample()) {
        // десериализует частицу из json строки
        // массивы соседей в json не входят

        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, ParticleDeserialiser()).create()
        val particle: Particle = gson.fromJson(json, getType())
        this.loc = particle.loc
        this.m = particle.m
        this.lma = particle.lma
        this.sample = sample
    }

    fun initializeNeighbors(dipolParticles: Set<Particle>,
                            exchangeParticles: Set<Particle>) {
        if (neighborsInitialized) {
            throw IllegalStateException("neighbors can be initialized only once")
        }
        this.dipolParticles = dipolParticles
        this.exchangeParticles = exchangeParticles
        neighborsInitialized = true
    }


    fun effectiveField(): Vector {
        TODO()
    }

    fun optimizeEnergy() {
        TODO()
    }

    fun computeEnergy(): Double {
        TODO()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Particle

        if (loc != other.loc) return false
        if (m != other.m) return false
        if (lma != other.lma) return false
//        if (sample != other.sample) return false
//        if (neighborsInitialized != other.neighborsInitialized) return false
//        if (dipolParticles != other.dipolParticles) return false
//        if (exchangeParticles != other.exchangeParticles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loc.hashCode()
        result = 31 * result + m.hashCode()
        result = 31 * result + lma.hashCode()
//        result = 31 * result + sample.hashCode()
//        result = 31 * result + neighborsInitialized.hashCode()
//        result = 31 * result + dipolParticles.hashCode()
//        result = 31 * result + exchangeParticles.hashCode()
        return result
    }

    fun toJsonString(): String {
        val gson = GsonBuilder().registerTypeAdapter(this.javaClass, ParticleSerializer()).setPrettyPrinting().create()
        val json = gson.toJson(this, getType())
        return json
    }

    fun getType(): Type {
        return TypeToken.get(this.javaClass).type
    }

}