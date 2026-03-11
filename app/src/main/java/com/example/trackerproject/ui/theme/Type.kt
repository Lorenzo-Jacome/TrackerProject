package com.example.trackerproject.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Bold,
        fontSize      = 18.sp,
        letterSpacing = 4.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Bold,
        fontSize      = 15.sp,
        letterSpacing = 3.sp,
    ),
    titleLarge = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Bold,
        fontSize      = 16.sp,
        letterSpacing = 2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        letterSpacing = 0.8.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        letterSpacing = 0.5.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Normal,
        fontSize      = 10.sp,
        letterSpacing = 0.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        letterSpacing = 2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Medium,
        fontSize      = 9.sp,
        letterSpacing = 2.sp,
    ),
)
