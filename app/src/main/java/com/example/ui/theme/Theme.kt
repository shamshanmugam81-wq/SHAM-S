package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmergencyRedPrimary,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedDark,
    onPrimaryContainer = Color.White,
    secondary = SafetyGreen,
    onSecondary = Color.White,
    secondaryContainer = OnSafetyGreenContainer,
    tertiary = WarningAmber,
    background = DarkSurface,
    onBackground = NeutralTextLight,
    surface = DarkCardSurface,
    onSurface = NeutralTextLight,
    surfaceVariant = Color(0xFF332A2C),
    onSurfaceVariant = Color(0xFFE0D0D2),
    error = Color(0xFFFF5252)
)

private val LightColorScheme = lightColorScheme(
    primary = EmergencyRedPrimary,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedContainer,
    onPrimaryContainer = OnEmergencyRedContainer,
    secondary = SafetyGreen,
    onSecondary = Color.White,
    secondaryContainer = SafetyGreenContainer,
    onSecondaryContainer = OnSafetyGreenContainer,
    tertiary = WarningAmber,
    background = LightSurface,
    onBackground = NeutralTextDark,
    surface = LightCardSurface,
    onSurface = NeutralTextDark,
    surfaceVariant = Color(0xFFF4ECEE),
    onSurfaceVariant = Color(0xFF534345),
    error = Color(0xFFD32F2F)
)

@Composable
fun SmartSosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our intentional SOS emergency branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
