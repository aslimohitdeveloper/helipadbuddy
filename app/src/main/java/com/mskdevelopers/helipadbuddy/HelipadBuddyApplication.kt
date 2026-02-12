package com.mskdevelopers.helipadbuddy

import android.app.Application
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import com.mskdevelopers.helipadbuddy.data.repository.PreferencesRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository

class HelipadBuddyApplication : Application() {
    val sensorRepository by lazy { SensorRepository(this) }
    val loggingRepository by lazy { LoggingRepository(this) }
    val preferencesRepository by lazy { PreferencesRepository(this) }
}
