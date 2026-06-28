package com.example.suckneting.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- Premium Dark Mode Palette ---
val DeepCharcoal = Color(0xFF121212)
val ElectricCyan = Color(0xFF00E5FF)
val CyberPurple = Color(0xFFAA00FF)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOutline = Color(0xFF333333)

// --- Ultra-Clean Light Mode Palette ---
val FrostWhite = Color(0xFFF5F7FA)
val DeepNavy = Color(0xFF1A237E)
val SoftSlate = Color(0xFF64748B)
val LightSurface = Color(0xFFFFFFFF)
val LightOutline = Color(0xFFE2E8F0)

// --- Specialized Gradients ---
val TechGradient = Brush.linearGradient(
    colors = listOf(ElectricCyan, CyberPurple)
)

val NavyGradient = Brush.linearGradient(
    colors = listOf(DeepNavy, Color(0xFF3949AB))
)

val DarkGlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF252525).copy(alpha = 0.9f),
        Color(0xFF121212).copy(alpha = 0.95f)
    )
)
