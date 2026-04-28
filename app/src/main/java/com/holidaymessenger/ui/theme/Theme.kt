package com.holidaymessenger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Festive Palette: Vibrant Red, Deep Green, Gold, and Winter Blue
private val FestiveLightColorScheme = lightColorScheme(
    primary = Color(0xFFC62828),      // Festive Red
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    secondary = Color(0xFF2E7D32),    // Holiday Green
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    tertiary = Color(0xFFF9A825),     // Celebration Gold
    onTertiary = Color.Black,
    surface = Color(0xFFFFFBFE),
    background = Color(0xFFFFF8E1),   // Warm Cream background
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val FestiveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5252),      // Bright Festive Red
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFD32F2F),
    secondary = Color(0xFF81C784),    // Soft Festive Green
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF388E3C),
    tertiary = Color(0xFFFFD54F),     // Bright Gold
    onTertiary = Color.Black,
    background = Color(0xFF001F11),   // Midnight Forest Green
    surface = Color(0xFF1B2E1D),      // Deep Evergreen surface
    onBackground = Color(0xFFE3F2FD),
    onSurface = Color(0xFFE3F2FD)
)

@Composable
fun HolidayMessengerTheme(
    darkTheme: Boolean = false, // Default to light/festive even if system is dark
    dynamicColor: Boolean = false, // Disable dynamic color to maintain festive branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FestiveDarkColorScheme else FestiveLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
