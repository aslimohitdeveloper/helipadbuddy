package com.mskdevelopers.helipadbuddy.data.model

enum class AlertSeverity {
    INFO,
    CAUTION,
    WARNING,
    CRITICAL
}

data class AlertData(
    val message: String,
    val severity: AlertSeverity
)
