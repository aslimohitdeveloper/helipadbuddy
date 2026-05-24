package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.local.FlightDataPoint
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReplayViewModel(
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    private val _points = MutableStateFlow<List<FlightDataPoint>>(emptyList())
    val points: StateFlow<List<FlightDataPoint>> = _points.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1)
    val playbackSpeed: StateFlow<Int> = _playbackSpeed.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            _points.value = loggingRepository.getPointsForSessionOnce(sessionId)
            _currentIndex.value = 0
        }
    }

    fun setIndex(index: Int) {
        val max = (_points.value.size - 1).coerceAtLeast(0)
        _currentIndex.value = index.coerceIn(0, max)
    }

    fun togglePlay() { _isPlaying.value = !_isPlaying.value }
    fun stop() { _isPlaying.value = false }

    fun setPlaybackSpeed(speed: Int) {
        _playbackSpeed.value = speed.coerceIn(1, 4)
    }

    fun advanceFrame() {
        val pts = _points.value
        if (pts.isEmpty()) return
        val next = _currentIndex.value + 1
        if (next >= pts.size) {
            _isPlaying.value = false
            _currentIndex.value = pts.size - 1
        } else {
            _currentIndex.value = next
        }
    }
}
