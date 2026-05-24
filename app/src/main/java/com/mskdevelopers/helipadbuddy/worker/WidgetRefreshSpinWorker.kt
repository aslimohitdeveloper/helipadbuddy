package com.mskdevelopers.helipadbuddy.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mskdevelopers.helipadbuddy.HelipadBuddyApplication
import com.mskdevelopers.helipadbuddy.data.repository.WidgetStateRepository
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidget
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidgetSmall

class WidgetRefreshSpinWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? HelipadBuddyApplication ?: return Result.failure()
        val state = WidgetStateRepository(applicationContext)
        if (!state.getCurrent().isRefreshing) {
            return Result.success()
        }
        app.weatherDataFetcher.incrementRefreshSpinFrame()
        HelipadWeatherWidget().updateAll(applicationContext)
        HelipadWeatherWidgetSmall().updateAll(applicationContext)
        if (state.getCurrent().isRefreshing) {
            WorkerScheduler.enqueueRefreshSpinTick(applicationContext)
        }
        return Result.success()
    }
}
