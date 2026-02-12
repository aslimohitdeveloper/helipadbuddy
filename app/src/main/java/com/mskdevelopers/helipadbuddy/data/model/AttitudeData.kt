package com.mskdevelopers.helipadbuddy.data.model

/**
 * Attitude (pitch and roll) and magnetic heading.
 * Used for artificial horizon and heading indicator.
 */
data class AttitudeData(
    val pitchDegrees: Float,
    val rollDegrees: Float,
    val headingDegrees: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = AttitudeData(
            pitchDegrees = 0f,
            rollDegrees = 0f,
            headingDegrees = 0f
        )
    }
}
