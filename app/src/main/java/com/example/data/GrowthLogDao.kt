package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthLogDao {
    @Query("SELECT * FROM growth_logs WHERE plantId = :plantId ORDER BY date ASC")
    fun getLogsForPlant(plantId: Int): Flow<List<GrowthLog>>

    @Insert
    suspend fun insertLog(log: GrowthLog)
}
