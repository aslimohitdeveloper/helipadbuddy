package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.WeatherRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.WeatherDerivedValues

@Composable
fun TemperatureWeatherCard(
    weather: WidgetWeatherData,
    pressure: PressureData,
    modifier: Modifier = Modifier
) {
    val temp = weather.temperature
    val dew = weather.dewPoint
    val wetBulb = WeatherDerivedValues.wetBulbC(temp, dew)
    val heatIdx = WeatherDerivedValues.heatIndexC(temp, weather.humidity)
    val vaporE = WeatherDerivedValues.vaporPressureHpa(temp, weather.humidity)
    val vaporEs = WeatherDerivedValues.saturationVaporPressureHpa(temp)
    val gpsModelNote = weather.weatherSource == WeatherRepository.WEATHER_SOURCE_METAR_CLOUDS ||
        weather.weatherSource == WeatherRepository.WEATHER_SOURCE_METAR_ENHANCED

    AviationInstrumentCard(style = InstrumentStyle.WEATHER_TEMP, modifier = modifier, contentPadding = 6.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("TEMPERATURE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        }
        Column {
            WeatherLine("Temp", "%.0f°C".format(temp))
            WeatherLine("Dew", "%.0f°C".format(dew))
            WeatherLine("RH", "${weather.humidity}%")
            if (vaporE > 0f) {
                WeatherLine("Vapor e", "%.1f hPa".format(vaporE))
            }
            if (vaporEs > 0f && vaporE > 0f) {
                WeatherLine("Vapor eₛ", "%.1f hPa".format(vaporEs))
            }
            WeatherLine("Wet bulb", "%.0f°C".format(wetBulb))
            WeatherLine("Heat Idx", "%.0f°C".format(heatIdx))
            if (gpsModelNote) {
                Text(
                    "Open-Meteo @ GPS (temp/wind/vis)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
