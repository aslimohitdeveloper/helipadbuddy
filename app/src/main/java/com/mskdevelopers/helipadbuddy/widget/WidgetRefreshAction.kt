package com.mskdevelopers.helipadbuddy.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mskdevelopers.helipadbuddy.worker.WidgetRefreshCoordinator

class WidgetRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetRefreshCoordinator.requestRefresh(context.applicationContext, glanceId)
    }
}
