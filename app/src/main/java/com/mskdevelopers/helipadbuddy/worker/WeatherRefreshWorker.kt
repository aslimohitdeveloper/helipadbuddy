package com.mskdevelopers.helipadbuddy.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mskdevelopers.helipadbuddy.HelipadBuddyApplication
import com.mskdevelopers.helipadbuddy.widget.WidgetLocationStore

class WeatherRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? HelipadBuddyApplication ?: return Result.failure()
        val loc = WidgetLocationStore.getLastLocation(applicationContext) ?: return Result.retry()
        return try {
            val pressure = app.pressureWidgetRepository.pressureWidgetData.value
            val pressureArg = if (pressure.qfeHpa > 0f) pressure else null
            app.weatherDataFetcher.refresh(
                latitude = loc.latitude,
                longitude = loc.longitude,
                altitudeMsl = loc.altitudeMsl,
                sats = loc.satelliteCount,
                gpsQuality = loc.gpsQuality,
                pressure = pressureArg,
                isRefreshing = false
            )
            if (pressure.qfeHpa > 0f) {
                app.pressureWidgetRepository.markWidgetPublished(pressure.qfeHpa)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            WidgetRefreshCoordinator.finishRefresh(applicationContext)
        }
    }
}
