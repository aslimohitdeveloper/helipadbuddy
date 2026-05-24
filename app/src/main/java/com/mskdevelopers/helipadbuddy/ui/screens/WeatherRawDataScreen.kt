package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.MetarRepository

@Composable
fun WeatherRawDataScreen(
    weather: WidgetWeatherData,
    pressure: PressureData,
    modifier: Modifier = Modifier
) {
    val metarRepo = MetarRepository()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        RawSection(title = "GPS / Open-Meteo") {
            if (weather.openMeteoDiagnostics.isNotBlank()) {
                Text(weather.openMeteoDiagnostics, fontFamily = FontFamily.Monospace)
            } else {
                Text("Lat: ${weather.latitude}, Lon: ${weather.longitude}")
                Text("Temp: ${weather.temperature}°C  Dew: ${weather.dewPoint}°C  RH: ${weather.humidity}%")
                Text("Wind: ${weather.windDirection}° @ ${weather.windSpeedKt} kt")
                Text("Vis: ${weather.visibilityKm} km  Cloud: ${weather.cloudCover}%")
                Text("Model QNH: ${weather.qnh} hPa")
            }
        }
        RawSection(title = "METAR (${weather.station})") {
            Text(weather.metarRaw.ifBlank { "No METAR fetched." }, fontFamily = FontFamily.Monospace)
            if (weather.metarRaw.isNotBlank()) {
                val metarWx = metarRepo.parseWeatherPhenomena(weather.metarRaw)
                Text("Parsed wx: ${metarWx.orEmpty().ifBlank { "—" }}")
                Text("METAR QNH: ${if (weather.qnhMetarHpa > 0f) "%.0f hPa".format(weather.qnhMetarHpa) else "—"}")
                Text("Cloud low/med/high: ${weather.cloudLow} / ${weather.cloudMedium} / ${weather.cloudHigh}")
                Text("Ceiling: ${if (weather.ceilingFt > 0) "${weather.ceilingFt} ft" else "—"}")
                Text("Note: Wind/vis in tiles use Open-Meteo at GPS, not METAR station values.")
            }
        }
        RawSection(title = "Merged snapshot") {
            Text("Source: ${weather.weatherSource}")
            Text("Updated: ${weather.updatedAtMillis}")
            Text("Station: ${weather.station}")
            Text("Condition: ${weather.weather}")
            Text("Temp ${weather.temperature}°C / Dew ${weather.dewPoint}°C / RH ${weather.humidity}%")
            Text("Wind ${weather.windDirection}° @ ${weather.windSpeedKt} kt")
            Text("Vis ${weather.visibilityKm} km / Cloud ${weather.cloudCover}%")
            Text("Model QNH ${weather.qnh} / METAR QNH ${weather.qnhMetarHpa} / Phone QNH ${weather.qnhPhoneHpa} / QFE ${weather.qfeHpa}")
            Text("GPS: ${weather.satelliteCount} sats ${weather.gpsQuality} @ ${weather.altitudeMsl} m MSL")
            weather.forecastPoints.forEach { p ->
                Text("  ${p.timeLabel}: ${p.temperature}°C ${p.windDirection}°@${p.windSpeedKt}kt ${p.precipProbability}%")
            }
        }
        RawSection(title = "Phone barometer") {
            Text("QFE: ${pressure.qfeHpa}")
            Text("QNH (phone): ${pressure.qnhHpa}")
            Text("Pressure alt: ${"%.0f".format(pressure.pressureAltitudeFeet)} ft")
            Text("Trend: ${pressure.trendDirection}")
        }
    }
}

@Composable
private fun RawSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Column(modifier = Modifier.padding(top = 8.dp)) {
                content()
            }
        }
    }
}
