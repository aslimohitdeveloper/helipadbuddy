package com.mskdevelopers.helipadbuddy.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.local.FlightSession
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

/**
 * Manages flight session recording and CSV export.
 */
class LoggingViewModel(
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<FlightSession>>(emptyList())
    val sessions: StateFlow<List<FlightSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    private val _exportResult = MutableStateFlow<Result<File>?>(null)
    val exportResult: StateFlow<Result<File>?> = _exportResult.asStateFlow()

    init {
        loggingRepository.getAllSessions()
            .onEach { _sessions.value = it }
            .launchIn(viewModelScope)
        loggingRepository.getActiveSession()
            .onEach { session -> _activeSessionId.value = session?.id }
            .launchIn(viewModelScope)
    }

    fun startSession() {
        viewModelScope.launch {
            val id = loggingRepository.startSession()
            _activeSessionId.value = id
        }
    }

    fun stopSession() {
        viewModelScope.launch {
            _activeSessionId.value?.let { id ->
                loggingRepository.stopSession(id)
                _activeSessionId.value = null
            }
        }
    }

    fun recordPoint(
        altitudeMeters: Double,
        groundSpeedKnots: Float,
        headingDegrees: Float,
        verticalSpeedFtMin: Float,
        gLoad: Float
    ) {
        viewModelScope.launch {
            _activeSessionId.value?.let { sessionId ->
                loggingRepository.addDataPoint(
                    sessionId = sessionId,
                    altitudeMeters = altitudeMeters,
                    groundSpeedKnots = groundSpeedKnots,
                    headingDegrees = headingDegrees,
                    verticalSpeedFtMin = verticalSpeedFtMin,
                    gLoad = gLoad
                )
            }
        }
    }

    fun exportToCsv(sessionId: Long, context: Context) {
        viewModelScope.launch {
            val file = File(context.getExternalFilesDir(null), "flight_${sessionId}.csv")
            val result = loggingRepository.exportSessionToCsv(sessionId, file)
            _exportResult.value = result
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }
}
