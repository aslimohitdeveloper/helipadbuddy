package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mskdevelopers.helipadbuddy.HelipadBuddyApplication
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import com.mskdevelopers.helipadbuddy.data.repository.PreferencesRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.data.repository.TerrainRepository
import com.mskdevelopers.helipadbuddy.data.repository.WeatherDataFetcher
import com.mskdevelopers.helipadbuddy.domain.alert.AlertEngine

class ViewModelFactory(
    private val application: HelipadBuddyApplication,
    private val sensorRepository: SensorRepository,
    private val loggingRepository: LoggingRepository,
    private val preferencesRepository: PreferencesRepository,
    private val terrainRepository: TerrainRepository,
    private val weatherDataFetcher: WeatherDataFetcher
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LocationViewModel::class.java) ->
                LocationViewModel(sensorRepository) as T
            modelClass.isAssignableFrom(PressureViewModel::class.java) ->
                PressureViewModel(sensorRepository, application.pressureWidgetRepository) as T
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
            modelClass.isAssignableFrom(WidgetSyncViewModel::class.java) ->
                WidgetSyncViewModel(
                    weatherDataFetcher,
                    application.pressureWidgetRepository,
                    application
                ) as T
            modelClass.isAssignableFrom(TerrainViewModel::class.java) ->
                TerrainViewModel(terrainRepository) as T
            modelClass.isAssignableFrom(WindViewModel::class.java) ->
                WindViewModel() as T
            modelClass.isAssignableFrom(AlertViewModel::class.java) ->
                AlertViewModel(AlertEngine()) as T
            modelClass.isAssignableFrom(ReplayViewModel::class.java) ->
                ReplayViewModel(loggingRepository) as T
            modelClass.isAssignableFrom(AnalyticsViewModel::class.java) ->
                AnalyticsViewModel(loggingRepository) as T
            modelClass.isAssignableFrom(SensorHealthViewModel::class.java) ->
                SensorHealthViewModel(sensorRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }

    companion object {
        fun from(application: HelipadBuddyApplication): ViewModelFactory =
            ViewModelFactory(
                application = application,
                sensorRepository = application.sensorRepository,
                loggingRepository = application.loggingRepository,
                preferencesRepository = application.preferencesRepository,
                terrainRepository = application.terrainRepository,
                weatherDataFetcher = application.weatherDataFetcher
            )
    }
}
