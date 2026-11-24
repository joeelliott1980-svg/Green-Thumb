package com.example.data

import kotlinx.coroutines.flow.Flow
class PlantRepository(
    private val plantDao: PlantDao,
    private val growthLogDao: GrowthLogDao
) {
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    suspend fun insert(plant: Plant) = plantDao.insertPlant(plant)

    suspend fun update(plant: Plant) = plantDao.updatePlant(plant)

    suspend fun delete(plant: Plant) = plantDao.deletePlant(plant)

    suspend fun getPlantById(id: Int) = plantDao.getPlantById(id)

    fun getLogsForPlant(plantId: Int) = growthLogDao.getLogsForPlant(plantId)

    suspend fun insertLog(log: GrowthLog) = growthLogDao.insertLog(log)
}
