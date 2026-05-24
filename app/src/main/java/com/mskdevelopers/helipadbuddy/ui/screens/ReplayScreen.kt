package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.local.FlightDataPoint
import com.mskdevelopers.helipadbuddy.ui.viewmodel.ReplayViewModel
import kotlinx.coroutines.delay

@Composable
fun ReplayScreen(
    viewModel: ReplayViewModel,
    sessionId: Long,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }
    val points by viewModel.points.collectAsState()
    val index by viewModel.currentIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()

    LaunchedEffect(isPlaying, speed, points.size) {
        if (!isPlaying || points.isEmpty()) return@LaunchedEffect
        while (viewModel.isPlaying.value) {
            delay((1000L / speed).coerceAtLeast(100L))
            viewModel.advanceFrame()
        }
    }

    val current = points.getOrNull(index)

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Flight Replay", style = MaterialTheme.typography.titleLarge)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF111111))
        ) {
            ReplayMapCanvas(points = points, currentIndex = index, modifier = Modifier.fillMaxSize())
            current?.let { pt ->
                Text(
                    "ALT %.0fm  SPD %.0fkt  HDG %.0f°  VSI %.0f".format(
                        pt.altitudeMslMeters.takeIf { it > 0 } ?: pt.altitudeMeters,
                        pt.groundSpeedKnots, pt.headingDegrees, pt.verticalSpeedFtMin
                    ),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    fontSize = 12.sp
                )
            }
        }
        if (points.isNotEmpty()) {
            Slider(
                value = index.toFloat(),
                onValueChange = { viewModel.setIndex(it.toInt()) },
                valueRange = 0f..(points.size - 1).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text("Frame ${index + 1} / ${points.size}  Speed ${speed}x", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReplayMapCanvas(points: List<FlightDataPoint>, currentIndex: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val lats = points.map { it.latitude }.filter { it != 0.0 }
        val lons = points.map { it.longitude }.filter { it != 0.0 }
        if (lats.size < 2) return@Canvas
        val minLat = lats.min(); val maxLat = lats.max()
        val minLon = lons.min(); val maxLon = lons.max()
        val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
        val lonRange = (maxLon - minLon).coerceAtLeast(0.0001)
        fun toXY(lat: Double, lon: Double): Offset {
            val x = ((lon - minLon) / lonRange * size.width * 0.8 + size.width * 0.1).toFloat()
            val y = ((1 - (lat - minLat) / latRange) * size.height * 0.8 + size.height * 0.1).toFloat()
            return Offset(x, y)
        }
        for (i in 0 until points.size - 1) {
            val a = points[i]; val b = points[i + 1]
            if (a.latitude == 0.0 || b.latitude == 0.0) continue
            drawLine(Color(0x4400BCD4), toXY(a.latitude, a.longitude), toXY(b.latitude, b.longitude), strokeWidth = 2f)
        }
        points.getOrNull(currentIndex)?.let { pt ->
            if (pt.latitude != 0.0) {
                drawCircle(Color(0xFFFF9800), radius = 10f, center = toXY(pt.latitude, pt.longitude))
            }
        }
    }
}
