package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mskdevelopers.helipadbuddy.data.model.RunwayConfig
import com.mskdevelopers.helipadbuddy.data.model.RunwayDashboardState
import com.mskdevelopers.helipadbuddy.data.model.RunwayDisplayStatus
import com.mskdevelopers.helipadbuddy.data.model.RunwayWindData
import com.mskdevelopers.helipadbuddy.data.model.WindData
import com.mskdevelopers.helipadbuddy.domain.calculation.RunwayWindCalculator
import com.mskdevelopers.helipadbuddy.domain.calculation.WindCalculationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WindViewModel : ViewModel() {

    private val runwayWindCalculator = RunwayWindCalculator()

    private val _wind = MutableStateFlow(WindData.EMPTY)
    val wind: StateFlow<WindData> = _wind.asStateFlow()

    private val _runwayDashboard = MutableStateFlow(RunwayDashboardState.EMPTY)
    val runwayDashboard: StateFlow<RunwayDashboardState> = _runwayDashboard.asStateFlow()

    fun update(headingDeg: Float, trackDeg: Float, groundSpeedKts: Float) {
        _wind.value = WindCalculationEngine.calculate(headingDeg, trackDeg, groundSpeedKts)
    }

    fun updateRunwayWind(
        runway: RunwayConfig?,
        windDirectionDeg: Int,
        windSpeedKts: Float
    ) {
        when {
            runway == null -> {
                _runwayDashboard.value = RunwayDashboardState(status = RunwayDisplayStatus.NO_RUNWAY)
            }
            !runway.hasValidHeading -> {
                _runwayDashboard.value = RunwayDashboardState(
                    status = RunwayDisplayStatus.CONFIGURE_RUNWAY,
                    activeRunway = runway
                )
            }
            windSpeedKts <= 0f -> {
                _runwayDashboard.value = RunwayDashboardState(
                    status = RunwayDisplayStatus.NO_WIND,
                    activeRunway = runway,
                    windDirectionDeg = windDirectionDeg
                )
            }
            else -> {
                val components = runwayWindCalculator.calculate(
                    windDirection = windDirectionDeg,
                    windSpeedKt = windSpeedKts,
                    runwayHeading = runway.activeHeadingDeg
                )
                _runwayDashboard.value = RunwayDashboardState(
                    status = RunwayDisplayStatus.READY,
                    activeRunway = runway,
                    windDirectionDeg = windDirectionDeg,
                    windSpeedKts = windSpeedKts,
                    components = components,
                    severity = runwayWindCalculator.crosswindSeverity(components.crosswindKt)
                )
            }
        }
    }

    fun currentRunwayWind(): RunwayWindData = _runwayDashboard.value.components
}
