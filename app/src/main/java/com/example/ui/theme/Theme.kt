package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LuxColorScheme =
  darkColorScheme(
    primary = LuxPrimary,
    onPrimary = LuxDarkBackground,
    primaryContainer = LuxSurfaceVariant,
    onPrimaryContainer = LuxPrimary,
    secondary = LuxSecondary,
    onSecondary = LuxDarkBackground,
    secondaryContainer = LuxSurface,
    onSecondaryContainer = LuxText,
    tertiary = LuxPrimary,
    background = LuxDarkBackground,
    onBackground = LuxText,
    surface = LuxSurface,
    onSurface = LuxText,
    surfaceVariant = LuxSurfaceVariant,
    onSurfaceVariant = LuxMutedText,
    error = LuxError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark luxury theme
  dynamicColor: Boolean = false, // Disable dynamic colors to keep luxurious style
  content: @Composable () -> Unit,
) {
  val colorScheme = LuxColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
