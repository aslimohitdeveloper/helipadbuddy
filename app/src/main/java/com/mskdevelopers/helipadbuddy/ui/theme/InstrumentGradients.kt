package com.mskdevelopers.helipadbuddy.ui.theme

import androidx.compose.ui.graphics.Color
import com.mskdevelopers.helipadbuddy.ui.components.InstrumentStyle

data class CardGradient(val start: Color, val end: Color)

object InstrumentGradients {
    val DashboardBackground = Color(0xFF071426)

    fun forStyle(style: InstrumentStyle): CardGradient = when (style) {
        InstrumentStyle.ALTITUDE -> CardGradient(Color(0xFF0D1B3E), Color(0xFF1E4DB7))
        InstrumentStyle.VSI -> CardGradient(Color(0xFF2A1454), Color(0xFF6A3FB8))
        InstrumentStyle.SPEED -> CardGradient(Color(0xFF0F3D2E), Color(0xFF1B8A5A))
        InstrumentStyle.PRESSURE -> CardGradient(Color(0xFF4A2800), Color(0xFFE65100))
        InstrumentStyle.COMPASS -> CardGradient(Color(0xFF0A1628), Color(0xFF1A237E))
        InstrumentStyle.GNSS -> CardGradient(Color(0xFF102A43), Color(0xFF243B53))
        InstrumentStyle.RUNWAY -> CardGradient(Color(0xFF0A0F1A), Color(0xFF1A237E))
        InstrumentStyle.MOTION -> CardGradient(Color(0xFF1A1A2E), Color(0xFF2D2D44))
        InstrumentStyle.NEUTRAL -> CardGradient(Color(0xFF121A2E), Color(0xFF1E2A45))
        InstrumentStyle.COORDINATES -> CardGradient(Color(0xFF0D1B3E), Color(0xFF1E4DB7))
        InstrumentStyle.WEATHER_WIND -> CardGradient(Color(0xFF0F3D2E), Color(0xFF1B8A5A))
        InstrumentStyle.WEATHER_TEMP -> CardGradient(Color(0xFF4A2800), Color(0xFFE65100))
        InstrumentStyle.WEATHER_PRESSURE -> CardGradient(Color(0xFF2A1454), Color(0xFF6A3FB8))
        InstrumentStyle.WEATHER_CLOUD -> CardGradient(Color(0xFF102A43), Color(0xFF243B53))
        InstrumentStyle.WEATHER_FORECAST -> CardGradient(Color(0xFF1A1030), Color(0xFF5E35B1))
    }
}
