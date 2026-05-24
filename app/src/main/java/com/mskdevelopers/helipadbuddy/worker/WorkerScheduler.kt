package com.mskdevelopers.helipadbuddy.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    private const val WEATHER_PERIODIC = "weather_refresh_periodic"
    private const val WEATHER_LOCATION = "weather_refresh_location"
    private const val WEATHER_IMMEDIATE = "weather_refresh_immediate"
    private const val WIDGET_REFRESH_SPIN = "widget_refresh_spin"

    fun schedulePeriodicWeatherRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(20, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEATHER_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun enqueueLocationWeatherRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<WeatherRefreshWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WEATHER_LOCATION,
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun enqueueRefreshSpinTick(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshSpinWorker>()
            .setInitialDelay(450, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WIDGET_REFRESH_SPIN,
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelRefreshSpin(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_REFRESH_SPIN)
    }

    fun enqueueImmediateWeatherRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<WeatherRefreshWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WEATHER_IMMEDIATE,
            androidx.work.ExistingWorkPolicy.KEEP,
            request
        )
    }
}
