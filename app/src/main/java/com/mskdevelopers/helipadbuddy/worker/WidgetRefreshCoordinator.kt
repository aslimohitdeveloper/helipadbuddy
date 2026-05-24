package com.mskdevelopers.helipadbuddy.worker

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.updateAll
import com.mskdevelopers.helipadbuddy.HelipadBuddyApplication
import com.mskdevelopers.helipadbuddy.data.repository.WidgetStateRepository
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidget
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidgetSmall

object WidgetRefreshCoordinator {

    private const val DEBOUNCE_MS = 2_000L

    @Volatile
    private var lastRequestAt = 0L

    suspend fun requestRefresh(context: Context, @Suppress("UNUSED_PARAMETER") glanceId: GlanceId? = null): Boolean {
        val app = context.applicationContext as HelipadBuddyApplication
        val now = System.currentTimeMillis()
        val debounced = synchronized(this) {
            if (now - lastRequestAt < DEBOUNCE_MS) {
                true
            } else {
                lastRequestAt = now
                false
            }
        }
        app.weatherDataFetcher.markRefreshing(true)
        WidgetStateRepository(context).setRefreshSpinFrame(0)
        HelipadWeatherWidget().updateAll(context)
        HelipadWeatherWidgetSmall().updateAll(context)
        if (debounced) {
            return false
        }
        WorkerScheduler.enqueueRefreshSpinTick(context)
        WorkerScheduler.enqueueImmediateWeatherRefresh(context)
        return true
    }

    suspend fun finishRefresh(context: Context) {
        val app = context.applicationContext as HelipadBuddyApplication
        app.weatherDataFetcher.markRefreshing(false)
        WorkerScheduler.cancelRefreshSpin(context)
        HelipadWeatherWidget().updateAll(context)
        HelipadWeatherWidgetSmall().updateAll(context)
    }
}
