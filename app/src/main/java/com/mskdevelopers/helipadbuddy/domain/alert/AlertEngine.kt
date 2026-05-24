package com.mskdevelopers.helipadbuddy.domain.alert

class AlertEngine(private val dedupWindowMs: Long = 3_000L) {

    private val lastTriggered = mutableMapOf<AlertType, Long>()

    fun shouldShow(type: AlertType, condition: Boolean, enabled: Boolean = true): Boolean {
        if (!enabled) return false
        if (!condition) {
            lastTriggered.remove(type)
            return false
        }
        val now = System.currentTimeMillis()
        val last = lastTriggered[type] ?: 0L
        if (now - last < dedupWindowMs) return true
        lastTriggered[type] = now
        return true
    }

    fun reset(type: AlertType) {
        lastTriggered.remove(type)
    }
}
