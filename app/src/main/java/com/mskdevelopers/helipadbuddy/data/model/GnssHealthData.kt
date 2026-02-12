package com.mskdevelopers.helipadbuddy.data.model

/**
 * GNSS health and satellite status.
 * Satellites in view, used in fix, constellations, average SNR, quality indicator.
 */
data class GnssHealthData(
    val satellitesInView: Int,
    val satellitesUsedInFix: Int,
    val gpsCount: Int,
    val galileoCount: Int,
    val glonassCount: Int,
    val beidouCount: Int,
    val averageSnrDbHz: Float,
    val quality: GnssQuality,
    val hasFix: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class GnssQuality {
        GOOD,
        MARGINAL,
        POOR,
        NO_FIX
    }

    companion object {
        val EMPTY = GnssHealthData(
            satellitesInView = 0,
            satellitesUsedInFix = 0,
            gpsCount = 0,
            galileoCount = 0,
            glonassCount = 0,
            beidouCount = 0,
            averageSnrDbHz = 0f,
            quality = GnssQuality.NO_FIX,
            hasFix = false
        )
    }
}
