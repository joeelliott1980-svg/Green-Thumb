package com.example.util

import android.content.Context
import kotlin.math.roundToInt

enum class TemperatureUnit(val symbol: String, val label: String) {
    FAHRENHEIT("°F", "Fahrenheit"),
    CELSIUS("°C", "Celsius");

    companion object {
        fun fromString(value: String?): TemperatureUnit {
            return if (value.equals("CELSIUS", ignoreCase = true)) CELSIUS else FAHRENHEIT
        }
    }
}

object TemperatureUtil {
    private const val PREFS_NAME = "green_thumb_temperature_prefs"
    private const val KEY_TEMP_UNIT = "key_temperature_unit"

    /**
     * Gets user-selected temperature unit, with Fahrenheit as default.
     */
    fun getPreferredUnit(context: Context): TemperatureUnit {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_TEMP_UNIT, TemperatureUnit.FAHRENHEIT.name)
        return TemperatureUnit.fromString(saved)
    }

    /**
     * Persists the preferred temperature unit.
     */
    fun setPreferredUnit(context: Context, unit: TemperatureUnit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TEMP_UNIT, unit.name).apply()
    }

    fun celsiusToFahrenheit(celsius: Float): Float = (celsius * 9f / 5f) + 32f

    fun fahrenheitToCelsius(fahrenheit: Float): Float = (fahrenheit - 32f) * 5f / 9f

    /**
     * Converts a canonical Celsius value to the selected unit value.
     */
    fun toUnitValue(celsius: Float, unit: TemperatureUnit): Float {
        return when (unit) {
            TemperatureUnit.FAHRENHEIT -> celsiusToFahrenheit(celsius)
            TemperatureUnit.CELSIUS -> celsius
        }
    }

    /**
     * Converts a value in the given unit back to canonical Celsius.
     */
    fun toCelsius(value: Float, unit: TemperatureUnit): Float {
        return when (unit) {
            TemperatureUnit.FAHRENHEIT -> fahrenheitToCelsius(value)
            TemperatureUnit.CELSIUS -> value
        }
    }

    /**
     * Formats a Celsius temperature into string with unit symbol (e.g. "72°F" or "22°C").
     */
    fun formatTemperature(celsius: Float, unit: TemperatureUnit): String {
        return when (unit) {
            TemperatureUnit.FAHRENHEIT -> "${celsiusToFahrenheit(celsius).roundToInt()}°F"
            TemperatureUnit.CELSIUS -> "${celsius.roundToInt()}°C"
        }
    }

    /**
     * Formats a value already converted to the given unit.
     */
    fun formatUnitValue(value: Float, unit: TemperatureUnit): String {
        return "${value.roundToInt()}${unit.symbol}"
    }
}
