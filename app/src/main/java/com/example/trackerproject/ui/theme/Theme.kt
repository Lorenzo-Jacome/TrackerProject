package com.example.trackerproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FuturisticColorScheme = darkColorScheme(
    primary                 = NeonCyan,
    onPrimary               = DeepBg,
    primaryContainer        = SurfaceVar,
    onPrimaryContainer      = NeonCyan,
    secondary               = NeonGreen,
    onSecondary             = DeepBg,
    secondaryContainer      = SurfaceVar,
    onSecondaryContainer    = NeonGreen,
    tertiary                = NeonRed,
    onTertiary              = DeepBg,
    error                   = NeonRed,
    onError                 = DeepBg,
    background              = DeepBg,
    onBackground            = OnBg,
    surface                 = SurfaceDark,
    onSurface               = OnBg,
    surfaceVariant          = SurfaceVar,
    onSurfaceVariant        = OnBg,
    surfaceContainer        = SurfaceVar,
    surfaceContainerLow     = SurfaceDark,
    surfaceContainerHigh    = Color(0xFF171B28),
    surfaceContainerHighest = Color(0xFF1C2133),
    surfaceContainerLowest  = DeepBg,
    outline                 = Outline,
    outlineVariant          = DimAccent,
    inverseSurface          = OnBg,
    inverseOnSurface        = DeepBg,
    inversePrimary          = Color(0xFF006070),
    surfaceTint             = NeonCyan,
)

@Composable
fun TrackerProjectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FuturisticColorScheme,
        typography  = Typography,
        content     = content
    )
}
