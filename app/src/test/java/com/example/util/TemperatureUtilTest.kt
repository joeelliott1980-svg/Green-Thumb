package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class TemperatureUtilTest {

    @Test
    fun testCelsiusToFahrenheitConversion() {
        assertEquals(32f, TemperatureUtil.celsiusToFahrenheit(0f), 0.01f)
        assertEquals(71.6f, TemperatureUtil.celsiusToFahrenheit(22f), 0.1f)
        assertEquals(86f, TemperatureUtil.celsiusToFahrenheit(30f), 0.01f)
        assertEquals(50f, TemperatureUtil.celsiusToFahrenheit(10f), 0.01f)
        assertEquals(212f, TemperatureUtil.celsiusToFahrenheit(100f), 0.01f)
    }

    @Test
    fun testFahrenheitToCelsiusConversion() {
        assertEquals(0f, TemperatureUtil.fahrenheitToCelsius(32f), 0.01f)
        assertEquals(22f, TemperatureUtil.fahrenheitToCelsius(71.6f), 0.1f)
        assertEquals(30f, TemperatureUtil.fahrenheitToCelsius(86f), 0.01f)
        assertEquals(10f, TemperatureUtil.fahrenheitToCelsius(50f), 0.01f)
    }

    @Test
    fun testFormatTemperatureDefaultFahrenheit() {
        val fahrenheitStr = TemperatureUtil.formatTemperature(22f, TemperatureUnit.FAHRENHEIT)
        assertEquals("72°F", fahrenheitStr)

        val freezingF = TemperatureUtil.formatTemperature(0f, TemperatureUnit.FAHRENHEIT)
        assertEquals("32°F", freezingF)
    }

    @Test
    fun testFormatTemperatureCelsius() {
        val celsiusStr = TemperatureUtil.formatTemperature(22f, TemperatureUnit.CELSIUS)
        assertEquals("22°C", celsiusStr)
    }

    @Test
    fun testEnumFromString() {
        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.fromString(null))
        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.fromString("FAHRENHEIT"))
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.fromString("CELSIUS"))
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.fromString("celsius"))
    }
}
