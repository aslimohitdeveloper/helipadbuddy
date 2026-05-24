package com.mskdevelopers.helipadbuddy.domain.alert

import com.mskdevelopers.helipadbuddy.data.model.AlertSeverity

enum class AlertType(val message: String, val severity: AlertSeverity) {
    SINK_RATE("Sink Rate High", AlertSeverity.WARNING),
    TERRAIN("Terrain Proximity", AlertSeverity.WARNING),
    HARD_LANDING("Hard Landing", AlertSeverity.WARNING),
    GPS_WEAK("GPS Weak", AlertSeverity.INFO),
    BANK_CAUTION("Bank Angle Caution", AlertSeverity.CAUTION),
    BANK_WARNING("Bank Angle High", AlertSeverity.WARNING),
    BANK_CRITICAL("Bank Angle Critical", AlertSeverity.CRITICAL),
    EXCESSIVE_DESCENT("Excessive Descent", AlertSeverity.WARNING),
    CROSSWIND_INCREASING("Crosswind Increasing", AlertSeverity.CAUTION)
}
