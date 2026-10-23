package com.paestudiar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PaesLight = lightColorScheme(
    primary = Color(0xFF1E4FA3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E4FF),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = Color(0xFF8A6100),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFF7F8FC)
)

private val PaesDark = darkColorScheme(
    primary = Color(0xFFAEC6FF),
    onPrimary = Color(0xFF002E6B),
    primaryContainer = Color(0xFF0F3A82),
    onPrimaryContainer = Color(0xFFD9E4FF),
    secondary = Color(0xFFF2C14E),
    background = Color(0xFF12151C),
    surface = Color(0xFF12151C)
)

object PaesColors {
    val Correct = Color(0xFF2E9E5B)
    val Wrong = Color(0xFFD64545)
    private val axis = listOf(
        Color(0xFF1E4FA3), // Números
        Color(0xFF7B4FD1), // Álgebra y funciones
        Color(0xFF1C9C8C), // Geometría
        Color(0xFFE0742B)  // Probabilidad y estadística
    )
    fun axisColor(index: Int): Color = axis[index % axis.size]
}

@Composable
fun PaesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) PaesDark else PaesLight,
        content = content
    )
}
