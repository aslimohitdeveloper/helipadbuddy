package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.TerrainData
import com.mskdevelopers.helipadbuddy.data.repository.TerrainRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import com.mskdevelopers.helipadbuddy.domain.calculation.TerrainCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerrainViewModel(
    private val terrainRepository: TerrainRepository
) : ViewModel() {

    private val _terrain = MutableStateFlow(TerrainData.EMPTY)
    val terrain: StateFlow<TerrainData> = _terrain.asStateFlow()

    private val aglHistory = ArrayDeque<Pair<Long, Double>>(20)
    private val lowAglThresholdFt = 150.0
    private val descentRateThresholdFtMin = -500f
    private val rapidClosureFtPerMin = 300.0
    private val minClosureHistoryMs = 15_000L

    fun update(
        mslMeters: Double,
        latitude: Double,
        longitude: Double,
        verticalSpeedFtMin: Float = 0f
    ) {
        if (!latitude.isFinite() || !longitude.isFinite() || mslMeters == 0.0) return
        viewModelScope.launch {
            val terrainM = terrainRepository.getTerrainElevationMeters(latitude, longitude)
            if (terrainM == null) {
                _terrain.value = TerrainData(
                    isAvailable = false,
                    warningLevel = TerrainData.TerrainWarning.NONE
                )
                return@launch
            }

            val aglM = TerrainCalculator.aglMeters(mslMeters, terrainM)
            val terrainFt = AviationFormulas.metersToFeet(terrainM)
            val aglFt = AviationFormulas.metersToFeet(aglM)

            val now = System.currentTimeMillis()
            aglHistory.addLast(now to aglFt)
            while (aglHistory.size > 20) aglHistory.removeFirst()

            var closureRateFtMin = 0.0
            var closureHistoryValid = false
            if (aglHistory.size >= 2) {
                val (t0, a0) = aglHistory.first()
                val (t1, a1) = aglHistory.last()
                val dtMin = (t1 - t0) / 60000.0
                closureHistoryValid = (t1 - t0) >= minClosureHistoryMs
                if (dtMin > 0) {
                    closureRateFtMin = (a0 - a1) / dtMin
                }
            }

            var warning = TerrainData.TerrainWarning.NONE
            val lowClearanceDescent = aglFt in 0.0..lowAglThresholdFt &&
                verticalSpeedFtMin < descentRateThresholdFtMin
            val rapidClosure = closureHistoryValid &&
                closureRateFtMin > rapidClosureFtPerMin &&
                aglFt > 0.0
            if (lowClearanceDescent) {
                warning = TerrainData.TerrainWarning.LOW_CLEARANCE
            }
            if (rapidClosure) {
                warning = TerrainData.TerrainWarning.RAPID_CLOSURE
            }

            _terrain.value = TerrainData(
                terrainElevationMeters = terrainM,
                terrainElevationFeet = terrainFt,
                aglMeters = aglM,
                aglFeet = aglFt,
                warningLevel = warning,
                isAvailable = true
            )
        }
    }
}
