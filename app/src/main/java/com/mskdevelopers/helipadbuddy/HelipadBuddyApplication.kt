package com.mskdevelopers.helipadbuddy

import android.app.Application
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import com.mskdevelopers.helipadbuddy.data.repository.PreferencesRepository
import com.mskdevelopers.helipadbuddy.data.repository.PressureWidgetRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.data.repository.TerrainRepository
import com.mskdevelopers.helipadbuddy.data.repository.WeatherDataFetcher
import com.mskdevelopers.helipadbuddy.worker.WorkerScheduler

class HelipadBuddyApplication : Application() {
    val sensorRepository by lazy { SensorRepository(this) }
    val loggingRepository by lazy { LoggingRepository(this) }
    val preferencesRepository by lazy { PreferencesRepository(this) }
    val terrainRepository by lazy { TerrainRepository(this) }
    val nearestAirportRepository by lazy { com.mskdevelopers.helipadbuddy.data.repository.NearestAirportRepository(this) }
    val weatherDataFetcher by lazy { WeatherDataFetcher(this, preferencesRepository) }
    val pressureWidgetRepository by lazy { PressureWidgetRepository() }

    override fun onCreate() {
        super.onCreate()
        WorkerScheduler.schedulePeriodicWeatherRefresh(this)
    }
}
