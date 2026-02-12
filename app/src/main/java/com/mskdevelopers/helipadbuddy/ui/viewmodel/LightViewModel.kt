package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.repository.LightRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Ambient light for auto night mode (red/dim aviation palette).
 */
class LightViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _isNightMode = MutableStateFlow(false)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _lux = MutableStateFlow(Float.MAX_VALUE)
    val lux: StateFlow<Float> = _lux.asStateFlow()

    /** Threshold below which we switch to night mode. */
    private val _nightThresholdLux = MutableStateFlow(LightRepository.DEFAULT_NIGHT_THRESHOLD_LUX)
    val nightThresholdLuxState: StateFlow<Float> = _nightThresholdLux.asStateFlow()

    fun setNightThresholdLux(lux: Float) {
        _nightThresholdLux.value = lux
        _isNightMode.value = _lux.value < lux
    }

    fun startCollecting() {
        sensorRepository.lightRepository.lightFlow()
            .onEach { luxValue ->
                _lux.value = luxValue
                _isNightMode.value = luxValue < _nightThresholdLux.value
            }
            .launchIn(viewModelScope)
    }
}
