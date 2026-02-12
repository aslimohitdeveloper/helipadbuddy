package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mskdevelopers.helipadbuddy.HelipadBuddyApplication
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import com.mskdevelopers.helipadbuddy.data.repository.PreferencesRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository

class ViewModelFactory(
    private val sensorRepository: SensorRepository,
    private val loggingRepository: LoggingRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LocationViewModel::class.java) ->
                LocationViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(PressureViewModel::class.java) ->
                PressureViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(VerticalPerformanceViewModel::class.java) ->
                VerticalPerformanceViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(GnssHealthViewModel::class.java) ->
                GnssHealthViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(AttitudeViewModel::class.java) ->
                AttitudeViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(MotionViewModel::class.java) ->
                MotionViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(LightViewModel::class.java) ->
                LightViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(LoggingViewModel::class.java) ->
                LoggingViewModel(loggingRepository) as T
            modelClass.isAssignableFrom(PreferencesViewModel::class.java) ->
                PreferencesViewModel(preferencesRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }

    companion object {
        fun from(application: HelipadBuddyApplication): ViewModelFactory =
            ViewModelFactory(
                application.sensorRepository,
                application.loggingRepository,
                application.preferencesRepository
            )
    }
}
