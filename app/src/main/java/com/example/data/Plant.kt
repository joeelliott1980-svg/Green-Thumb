package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val healthScore: Int,
    val lastWatered: Long,
    val nextWaterDate: Long,
    val species: String,
    val sunlight: Int,
    val temperature: Float,
    val humidity: Float,
    val carePlan: String = "",
    val growthSimulation: String = "",
    val imageUrl: String = ""
) : Serializable
