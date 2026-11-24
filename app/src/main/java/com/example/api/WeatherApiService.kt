package com.example.api

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val main: Main = Main(),
    val weather: List<Weather> = emptyList(),
    val name: String? = null
) {
    val isCurrentlyRaining: Boolean
        get() = weather.any { it.isRain }
}

data class Main(
    val temp: Float = 20.0f,
    val humidity: Int = 50
)

data class Weather(
    val description: String = "",
    val main: String? = null,
    val id: Int? = null,
    val icon: String? = null
) {
    val isRain: Boolean
        get() {
            val mainCheck = main?.equals("Rain", ignoreCase = true) == true ||
                    main?.equals("Drizzle", ignoreCase = true) == true ||
                    main?.equals("Thunderstorm", ignoreCase = true) == true
            val descCheck = description.contains("rain", ignoreCase = true) ||
                    description.contains("drizzle", ignoreCase = true) ||
                    description.contains("shower", ignoreCase = true) ||
                    description.contains("thunderstorm", ignoreCase = true)
            val idCheck = id != null && id in 200..599
            return mainCheck || descCheck || idCheck
        }
}

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse
}
