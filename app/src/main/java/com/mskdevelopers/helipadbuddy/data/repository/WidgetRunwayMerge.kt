package com.mskdevelopers.helipadbuddy.data.repository

import com.mskdevelopers.helipadbuddy.data.model.RunwayConfig
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.domain.calculation.RunwayWindCalculator
import kotlin.math.abs

internal object WidgetRunwayMerge {

    private val calculator = RunwayWindCalculator()

    fun applyRunway(
        data: WidgetWeatherData,
        activeRunway: RunwayConfig?,
        previous: WidgetWeatherData
    ): WidgetWeatherData {
        val runway = activeRunway?.takeIf { it.hasValidHeading }
        if (runway == null) {
            if (previous.runwayConfigured && previous.activeRunwayEnd.isNotEmpty()) {
                return data.copy(
                    activeRunwayEnd = previous.activeRunwayEnd,
                    headwindKt = previous.headwindKt,
                    tailwindKt = previous.tailwindKt,
                    crosswindKt = previous.crosswindKt,
                    crosswindSide = previous.crosswindSide,
                    runwayConfigured = true,
                    alertSeverity = previous.alertSeverity.ifBlank { data.alertSeverity }
                )
            }
            return data
        }
        val components = calculator.calculate(
            windDirection = data.windDirection,
            windSpeedKt = data.windSpeedKt.toFloat(),
            runwayHeading = runway.activeHeadingDeg
        )
        val side = when (components.crosswindDirection) {
            "Right" -> "R"
            "Left" -> "L"
            else -> ""
        }
        return data.copy(
            activeRunwayEnd = runway.activeRunway,
            headwindKt = components.headwindKt,
            tailwindKt = components.tailwindKt,
            crosswindKt = abs(components.crosswindKt),
            crosswindSide = side,
            runwayConfigured = true,
            alertSeverity = previous.alertSeverity.ifBlank { data.alertSeverity }
        )
    }
}
