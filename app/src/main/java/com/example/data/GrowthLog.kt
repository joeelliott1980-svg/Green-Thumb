package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "growth_logs",
    indices = [androidx.room.Index("plantId")],
    foreignKeys = [ForeignKey(
        entity = Plant::class,
        parentColumns = ["id"],
        childColumns = ["plantId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GrowthLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val date: Long,
    val heightCm: Float,
    val leafCount: Int,
    val isOutdoor: Boolean = false
)
