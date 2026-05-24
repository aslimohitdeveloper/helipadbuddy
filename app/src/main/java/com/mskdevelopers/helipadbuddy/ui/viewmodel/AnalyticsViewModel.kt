package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.local.FlightDataPoint
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    private val _points = MutableStateFlow<List<FlightDataPoint>>(emptyList())
    val points: StateFlow<List<FlightDataPoint>> = _points.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            _points.value = loggingRepository.getPointsForSessionOnce(sessionId)
        }
    }
}
