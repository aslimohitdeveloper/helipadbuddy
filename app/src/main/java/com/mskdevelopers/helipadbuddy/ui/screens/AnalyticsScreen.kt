package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.local.FlightDataPoint
import com.mskdevelopers.helipadbuddy.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    sessionId: Long,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }
    val points by viewModel.points.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Session Analytics", style = MaterialTheme.typography.titleLarge)
        if (points.isEmpty()) {
            Text("No data points recorded.", modifier = Modifier.padding(top = 16.dp))
            return
        }
        SimpleLineChart("Altitude (m MSL)", points) {
            (it.altitudeMslMeters.takeIf { v -> v > 0 } ?: it.altitudeMeters).toFloat()
        }
        SimpleLineChart("Ground Speed (kt)", points) { it.groundSpeedKnots }
        SimpleLineChart("VSI (ft/min)", points) { it.verticalSpeedFtMin }
        SimpleLineChart("G-Load", points) { it.gLoad }
    }
}

@Composable
private fun SimpleLineChart(
    title: String,
    points: List<FlightDataPoint>,
    valueOf: (FlightDataPoint) -> Float
) {
    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
    val values = points.map(valueOf)
    if (values.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 1f
        val range = (max - min).coerceAtLeast(0.001f)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i.toFloat() / (values.size - 1).coerceAtLeast(1) * size.width
            val y = size.height - ((v - min) / range * size.height * 0.9f + size.height * 0.05f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF00BCD4), style = Stroke(width = 3f))
        drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)
    }
}
