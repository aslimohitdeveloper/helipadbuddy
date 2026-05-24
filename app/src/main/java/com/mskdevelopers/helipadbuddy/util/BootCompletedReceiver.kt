package com.mskdevelopers.helipadbuddy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidget
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidgetSmall
import com.mskdevelopers.helipadbuddy.worker.WorkerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        WorkerScheduler.schedulePeriodicWeatherRefresh(appContext)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                HelipadWeatherWidget().updateAll(appContext)
                HelipadWeatherWidgetSmall().updateAll(appContext)
            } catch (_: Exception) {
            }
        }
    }
}
