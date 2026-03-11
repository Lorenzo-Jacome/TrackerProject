package com.example.trackerproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary                 = HotPink,
    onPrimary               = Color.Black,
    secondary               = NeonLime,
    onSecondary             = Color.Black,
    background              = Color.Black,
    onBackground            = Color.White,
    surface                 = Color.Black,
    onSurface               = Color.White,
    surfaceVariant          = Color(0xFF111111),
    onSurfaceVariant        = Color.White,
    surfaceContainer        = Color(0xFF111111),
    surfaceContainerLow     = Color(0xFF0A0A0A),
    surfaceContainerHigh    = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF222222),
    surfaceContainerLowest  = Color.Black,
    outline                 = Color(0xFF444444),
    outlineVariant          = Color(0xFF333333),
    error                   = Color(0xFFFF3355),
    onError                 = Color.Black,
)

@Composable
fun TrackerProjectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}
