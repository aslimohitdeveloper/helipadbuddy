package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.ui.theme.InstrumentGradients

enum class InstrumentStyle {
    ALTITUDE,
    VSI,
    SPEED,
    PRESSURE,
    COMPASS,
    GNSS,
    RUNWAY,
    MOTION,
    NEUTRAL,
    COORDINATES,
    WEATHER_WIND,
    WEATHER_TEMP,
    WEATHER_CLOUD,
    WEATHER_FORECAST,
    WEATHER_PRESSURE
}

@Composable
fun AviationInstrumentCard(
    style: InstrumentStyle,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    contentPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val gradient = InstrumentGradients.forStyle(style)
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = gradient.end.copy(alpha = 0.35f),
            spotColor = gradient.start.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .clipToBounds()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(gradient.start, gradient.end)
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxHeight()
                    .clipToBounds()
            ) {
                content()
            }
        }
    }
}
