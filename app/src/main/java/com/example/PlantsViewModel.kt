package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Plant
import com.example.data.GrowthLog
import com.example.data.PlantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantsViewModel(private val repository: PlantRepository) : ViewModel() {
    val plants: StateFlow<List<Plant>> = repository.allPlants
         .stateIn(
             scope = viewModelScope,
             started = SharingStarted.WhileSubscribed(5000),
             initialValue = emptyList()
         )

    fun addPlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(plant)
        }
    }

    fun updatePlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(plant)
        }
    }

    fun addLog(log: GrowthLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLog(log)
        }
    }

    fun getLogsForPlant(plantId: Int) = repository.getLogsForPlant(plantId)
    
    fun deletePlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(plant)
        }
    }

    class Factory(private val repository: PlantRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlantsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlantsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
