package com.mskdevelopers.helipadbuddy.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mskdevelopers.helipadbuddy.data.model.TrendDirection
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.WeatherRepository

class HelipadWeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val safeData = runCatching {
            loadWidgetData(context)
        }.getOrElse {
            fallbackWidgetData()
        }.let { data ->
            if (data.shouldShowFallback()) fallbackWidgetData() else data
        }
        provideContent {
            GlanceTheme {
                WidgetRootContent(data = safeData, isCompact = false)
            }
        }
    }
}

@Composable
internal fun WidgetRootContent(data: WidgetWeatherData, isCompact: Boolean) {
    when {
        data.shouldShowFallback() && !data.isRefreshing -> WidgetFallbackContent(isCompact = isCompact)
        data.shouldShowFallback() && data.isRefreshing -> WidgetLoadingContent(isCompact = isCompact)
        else -> WeatherWidgetContent(data = data, isCompact = isCompact)
    }
}

@Composable
internal fun WidgetLoadingContent(isCompact: Boolean) {
    val accent = ColorProvider(Color(0xFF00BCD4))
    val muted = ColorProvider(Color(0xFFAAAAAA))
    Column(
        modifier = widgetShellModifier(isCompact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Helipad Buddy",
            style = TextStyle(color = accent, fontSize = if (isCompact) 13.sp else 16.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            "⟳ Refreshing…",
            style = TextStyle(color = muted, fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
internal fun WidgetFallbackContent(isCompact: Boolean) {
    val accent = ColorProvider(Color(0xFF00BCD4))
    val muted = ColorProvider(Color(0xFFAAAAAA))
    val orange = ColorProvider(Color(0xFFFF9800))
    Column(
        modifier = widgetShellModifier(isCompact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Helipad Buddy",
            style = TextStyle(color = accent, fontSize = if (isCompact) 13.sp else 16.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            "Loading...",
            style = TextStyle(color = muted, fontSize = if (isCompact) 10.sp else 12.sp)
        )
        WidgetRefreshControl(isRefreshing = false, spinFrame = 0, isCompact = isCompact, accent = orange)
    }
}

@Composable
internal fun WeatherWidgetContent(data: WidgetWeatherData, isCompact: Boolean) {
    val muted = ColorProvider(Color(0xFFAAAAAA))
    val orange = ColorProvider(Color(0xFFFF9800))
    val white = ColorProvider(Color.White)
    val glass = ColorProvider(Color(0xFF1A1A2E))
    val accent = ColorProvider(Color(0xFF00BCD4))
    val red = ColorProvider(Color(0xFFE53935))

    Column(
        modifier = widgetShellModifier(isCompact),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (isCompact) {
            WidgetCompactContent(data, accent, orange, white)
        } else {
            WidgetHeaderRow(muted, orange, data.isRefreshing, data.refreshSpinFrame)
            WidgetAltitudeLine(data, glass, accent, muted)
            Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)) {
                WidgetWindLine(data, glass, white, modifier = GlanceModifier.defaultWeight())
                WidgetPressureLine(data, glass, white, muted, modifier = GlanceModifier.defaultWeight().padding(start = 8.dp))
            }
            WidgetRunwayLine(data, glass, white, muted)
            WidgetFooterLine(data, accent, red)
        }
    }
}

@Composable
private fun WidgetCompactContent(
    data: WidgetWeatherData,
    accent: ColorProvider,
    orange: ColorProvider,
    white: ColorProvider
) {
    val statusLine = buildString {
        append("ALT ${data.altitudeMsl}m • ")
        append("${data.windDirection}°@${data.windSpeedKt}kt")
        if (data.weather.isNotBlank()) append(" • ${data.weather}")
        if (data.cloudCover > 0) append(" • ${data.cloudCover}%")
        if (data.runwayConfigured && data.activeRunwayEnd.isNotEmpty()) {
            append(" • RWY${data.activeRunwayEnd}")
        }
    }
    Text(
        "Helipad Buddy",
        style = TextStyle(color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    )
    Text(
        statusLine,
        style = TextStyle(color = white, fontSize = 10.sp)
    )
    WidgetRefreshControl(
        isRefreshing = data.isRefreshing,
        spinFrame = data.refreshSpinFrame,
        isCompact = true,
        accent = orange
    )
}

@Composable
private fun WidgetHeaderRow(
    muted: ColorProvider,
    orange: ColorProvider,
    isRefreshing: Boolean,
    spinFrame: Int
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Helipad Buddy",
            style = TextStyle(color = muted, fontSize = 10.sp),
            modifier = GlanceModifier.defaultWeight()
        )
        WidgetRefreshControl(
            isRefreshing = isRefreshing,
            spinFrame = spinFrame,
            isCompact = false,
            accent = orange
        )
    }
}

private val REFRESH_SPIN_GLYPHS = arrayOf("↻", "↺", "⟳", "↻")

@Composable
private fun WidgetRefreshControl(
    isRefreshing: Boolean,
    spinFrame: Int,
    isCompact: Boolean,
    accent: ColorProvider
) {
    val muted = ColorProvider(Color(0xFFAAAAAA))
    if (isRefreshing) {
        val glyph = REFRESH_SPIN_GLYPHS[spinFrame.coerceIn(0, REFRESH_SPIN_GLYPHS.lastIndex)]
        Text(
            text = if (isCompact) "$glyph Refreshing…" else glyph,
            style = TextStyle(
                color = muted,
                fontSize = if (isCompact) 10.sp else 14.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier.padding(horizontal = 4.dp)
        )
    } else {
        Text(
            text = if (isCompact) "↻ Tap refresh" else "↻",
            style = TextStyle(
                color = accent,
                fontSize = if (isCompact) 10.sp else 14.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier.clickable(actionRunCallback<WidgetRefreshAction>())
        )
    }
}

@Composable
private fun WidgetAltitudeLine(
    data: WidgetWeatherData,
    glass: ColorProvider,
    accent: ColorProvider,
    muted: ColorProvider
) {
    val condition = data.weather.ifBlank { "—" }
    val detail = if (data.weatherSource.isNotEmpty()) data.weatherSource else "MSL"
    Text(
        "ALT ${data.altitudeMsl}m • $detail",
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(glass)
            .cornerRadius(16.dp)
            .padding(10.dp),
        style = TextStyle(color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    )
    Text(
        "$condition • Cloud ${data.cloudCover}%",
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        style = TextStyle(color = muted, fontSize = 10.sp)
    )
}

@Composable
private fun WidgetWindLine(
    data: WidgetWeatherData,
    glass: ColorProvider,
    white: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    val windLine = "${data.windDirection}° @ ${data.windSpeedKt}kt • T ${"%.0f".format(data.temperature)}°C • VIS ${"%.0f".format(data.visibilityKm)}km"
    Text(
        windLine,
        modifier = modifier
            .padding(top = 8.dp)
            .background(glass)
            .cornerRadius(12.dp)
            .padding(8.dp),
        style = TextStyle(color = white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun WidgetPressureLine(
    data: WidgetWeatherData,
    glass: ColorProvider,
    white: ColorProvider,
    muted: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    val pressureLine = buildString {
        if (data.qfeHpa > 0f) append("QFE ${"%.0f".format(data.qfeHpa)}")
        val phoneQnh = data.qnhPhoneHpa.takeIf { it > 0f }
        if (phoneQnh != null) {
            if (isNotEmpty()) append(" • ")
            append("Phone QNH ${"%.0f".format(phoneQnh)}")
        }
        if (data.qnh > 0f) {
            if (isNotEmpty()) append(" • ")
            append("Model QNH ${"%.0f".format(data.qnh)}")
        }
        if (data.qfeHpa <= 0f && phoneQnh == null) append("No barometer")
        if (data.pressureTrend != 0f || data.pressureTrendDirection != TrendDirection.STABLE.name) {
            append(" • ${trendArrow(data.pressureTrendDirection)} ${formatTrend(data.pressureTrend)}")
        }
    }
    Text(
        pressureLine,
        modifier = modifier
            .padding(top = 8.dp)
            .background(glass)
            .cornerRadius(12.dp)
            .padding(8.dp),
        style = TextStyle(
            color = if (data.qfeHpa > 0f || data.qnhPhoneHpa > 0f) white else muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun WidgetRunwayLine(
    data: WidgetWeatherData,
    glass: ColorProvider,
    white: ColorProvider,
    muted: ColorProvider
) {
    val runwayLine = if (data.runwayConfigured && data.activeRunwayEnd.isNotEmpty()) {
        "RWY ${data.activeRunwayEnd} • HW ${formatSignedKt(data.headwindKt)} • XW ${formatCrosswindWidget(data)}"
    } else {
        "Runway not configured"
    }
    Text(
        runwayLine,
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(glass)
            .cornerRadius(12.dp)
            .padding(8.dp),
        style = TextStyle(
            color = if (data.runwayConfigured) white else muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun WidgetFooterLine(
    data: WidgetWeatherData,
    accent: ColorProvider,
    red: ColorProvider
) {
    val footerText = buildString {
        if (data.alertSeverity.isNotEmpty()) {
            append("ALERT ${data.alertSeverity} • ")
        }
        append("GPS ${data.satelliteCount} SAT • ${data.gpsQuality}")
        if (data.metarRaw.isNotBlank()) {
            append(" • METAR ${data.station}")
        }
    }
    Text(
        footerText,
        modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
        style = TextStyle(
            color = if (data.alertSeverity.isNotEmpty()) red else accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

private fun widgetShellModifier(isCompact: Boolean): GlanceModifier {
    val bg = ColorProvider(Color(0xFF111111))
    return GlanceModifier
        .fillMaxSize()
        .background(bg)
        .cornerRadius(if (isCompact) 16.dp else 28.dp)
        .padding(12.dp)
}

private fun trendArrow(direction: String): String = when (direction) {
    TrendDirection.RISING.name -> "↑"
    TrendDirection.FALLING.name -> "↓"
    else -> "→"
}

private fun formatTrend(trend: Float): String {
    val sign = if (trend > 0f) "+" else ""
    return "$sign%.1f".format(trend)
}

private fun formatSignedKt(kt: Float): String {
    if (kt < 0.5f) return "0"
    return "+%.0f".format(kt)
}

private fun formatCrosswindWidget(data: WidgetWeatherData): String {
    if (data.crosswindKt < 0.5f) return "0 kt"
    val side = when (data.crosswindSide) {
        "R" -> "Right"
        "L" -> "Left"
        else -> ""
    }
    return "%.0f kt %s".format(data.crosswindKt, side).trim()
}
