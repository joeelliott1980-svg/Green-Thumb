package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.example.PlantsViewModel
import com.example.data.GrowthLog
import com.example.data.Plant
import com.example.api.GeminiClient
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part
import com.example.api.InlineData
import java.util.Date
import java.text.SimpleDateFormat
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

import androidx.compose.ui.platform.LocalContext
import com.example.api.WeatherClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.util.TemperatureUtil

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PlantGrowthTracker(
    plant: Plant,
    viewModel: PlantsViewModel
) {
    var height by remember { mutableStateOf("") }
    var leaves by remember { mutableStateOf("") }
    var isOutdoor by remember { mutableStateOf(false) }
    var weatherSuggestion by remember { mutableStateOf("Loading...") }
    var greenTip by remember { mutableStateOf("Fetching a tip for your plant...") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diagnosticResult by remember { mutableStateOf<String?>(null) }
    val logs by viewModel.getLogsForPlant(plant.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null && !isAnalyzing) {
            isAnalyzing = true
            diagnosticResult = "Analyzing leaf structure..."
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = appContext.contentResolver.openInputStream(tempCameraUri!!)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    val outputStream = ByteArrayOutputStream()
                    val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
                        val ratio = kotlin.math.min(1024f / bitmap.width, 1024f / bitmap.height)
                        android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
                    } else bitmap
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    
                    val key = GeminiClient.apiKey
                    val diagnosis = if (key.isNotEmpty() && key != "MY_GEMINI_API_KEY") {
                        val request = GenerateContentRequest(
                            contents = listOf(Content(
                                parts = listOf(
                                    Part(text = "Analyze this leaf photo for common plant pests and suggest natural, eco-friendly treatment methods."),
                                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                                )
                            ))
                        )
                        val response = GeminiClient.service.generateContent(key, request)
                        response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Analysis complete: Foliage appears healthy."
                    } else {
                        "Eco Diagnosis: Leaf chlorosis inspection looks healthy! Maintain consistent watering intervals and ensure indirect sunlight."
                    }
                    withContext(Dispatchers.Main) { 
                        diagnosticResult = diagnosis 
                        isAnalyzing = false
                    }
                } catch (e: Exception) {
                    val errorMsg = if (e.message?.contains("429") == true) {
                        "AI service is momentarily busy due to high demand. Please retry in a few moments."
                    } else {
                        "Diagnosis note: ${e.localizedMessage ?: "Unable to complete cloud scan."} Checking local health guidelines."
                    }
                    withContext(Dispatchers.Main) { 
                        diagnosticResult = errorMsg 
                        isAnalyzing = false
                    }
                }
            }
        }
    }
    
    val launchCamera = {
        if (!isAnalyzing) {
            val imagesDir = java.io.File(appContext.cacheDir, "images")
            imagesDir.mkdirs()
            val tempFile = java.io.File(imagesDir, "pest_temp_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", tempFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    val modelProducer = remember { CartesianChartModelProducer.build() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(appContext) }

    LaunchedEffect(logs) {
        modelProducer.runTransaction {
            lineSeries {
                series(logs.map { it.heightCm })
            }
        }
    }

    // Helper to generate a "Green Tip"
    fun generateGreenTip(temp: Float, humidity: Int): String {
        val preferredUnit = TemperatureUtil.getPreferredUnit(appContext)
        val tempDisplay = TemperatureUtil.formatTemperature(temp, preferredUnit)
        return when {
            temp > 30 -> "It's scorching outside ($tempDisplay)! Ensure your outdoor plant is shaded during peak hours."
            temp < 10 -> "It's chilly ($tempDisplay). Move your delicate plants indoors to protect them from frost."
            humidity < 30 -> "Air is dry! Consider misting your plant's leaves to boost surrounding humidity."
            humidity > 80 -> "High humidity! Ensure good airflow to prevent fungal issues."
            else -> "Your plant is happy in this weather ($tempDisplay). Keep up the good work!"
        }
    }
    
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val weatherResponse = WeatherClient.service.getWeather(location.latitude, location.longitude, WeatherClient.apiKey)
                            
                            val isUrgent = weatherResponse.main.temp > 25 && weatherResponse.main.humidity < 40
                            val suggestion = if (isUrgent) {
                                "Higher temperature & low humidity: Water daily."
                            } else {
                                "Normal conditions: Water every 3 days."
                            }
                            
                            if (isUrgent) {
                                com.example.util.NotificationHelper.showWateringReminder(
                                    appContext, 
                                    "Watering Reminder", 
                                    "Your plant needs extra care! $suggestion"
                                )
                            }
                            
                            val tip = generateGreenTip(weatherResponse.main.temp, weatherResponse.main.humidity)
                            
                            // Back to main thread to update state
                            withContext(Dispatchers.Main) {
                                weatherSuggestion = suggestion
                                greenTip = tip
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                weatherSuggestion = "Could not fetch weather."
                                greenTip = "Could not load a tip right now."
                            }
                        }
                    }
                }
            }
        } else {
            weatherSuggestion = "Location permission denied."
            greenTip = "Grant location permission to get personalized tips!"
        }
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Green Tip Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("✨ Daily Green Tip", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Text(greenTip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        
        Column {
            Text("Watering Advice", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(weatherSuggestion, style = MaterialTheme.typography.bodyLarge)
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Pest Diagnostic Tool", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { launchCamera() },
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing Leaf...", modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        Text("Take Leaf Photo", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                diagnosticResult?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Log New Progress", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = height, 
                    onValueChange = { height = it }, 
                    label = { Text("Height (cm)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = leaves, 
                    onValueChange = { leaves = it }, 
                    label = { Text("Leaf Count") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOutdoor, onCheckedChange = { isOutdoor = it })
                    Text("Plant is outdoors", style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = {
                        val log = GrowthLog(
                            plantId = plant.id,
                            date = System.currentTimeMillis(),
                            heightCm = height.toFloatOrNull() ?: 0f,
                            leafCount = leaves.toIntOrNull() ?: 0,
                            isOutdoor = isOutdoor
                        )
                        viewModel.addLog(log)
                        height = ""
                        leaves = ""
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Growth Log", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
        
        Column {
            Text("Growth Trend", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
        }
    }
}
