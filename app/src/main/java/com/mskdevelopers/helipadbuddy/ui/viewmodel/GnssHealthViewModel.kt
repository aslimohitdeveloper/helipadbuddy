package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * GNSS health: satellites in view, used, constellations, SNR, quality.
 */
class GnssHealthViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _gnssHealth = MutableStateFlow(GnssHealthData.EMPTY)
    val gnssHealth: StateFlow<GnssHealthData> = _gnssHealth.asStateFlow()

    private var gnssJob: Job? = null

    fun startCollecting() {
        gnssJob?.cancel()
        gnssJob = sensorRepository.locationRepository.gnssStatusFlow()
            .onEach { _gnssHealth.value = it }
            .launchIn(viewModelScope)
    }
}
