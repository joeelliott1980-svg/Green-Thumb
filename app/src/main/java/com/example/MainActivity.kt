package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PlantRepository
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = PlantRepository(database.plantDao(), database.growthLogDao())
        val factory = PlantsViewModel.Factory(repository)

        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val plantsViewModel: PlantsViewModel = viewModel(factory = factory)
                var showSplash by remember { mutableStateOf(true) }

                Surface(color = MaterialTheme.colorScheme.background) {
                    Box {
                        PlantHealthApp(viewModel = plantsViewModel)
                        if (showSplash) {
                            SplashScreen { showSplash = false }
                        }
                    }
                }
            }
        }
    }
}
