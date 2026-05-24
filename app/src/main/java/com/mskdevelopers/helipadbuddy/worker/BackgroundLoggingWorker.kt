package com.mskdevelopers.helipadbuddy.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mskdevelopers.helipadbuddy.HelipadBuddyApplication

class BackgroundLoggingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? HelipadBuddyApplication ?: return Result.failure()
        val session = app.loggingRepository.getActiveSessionOnce() ?: return Result.success()
        return try {
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
