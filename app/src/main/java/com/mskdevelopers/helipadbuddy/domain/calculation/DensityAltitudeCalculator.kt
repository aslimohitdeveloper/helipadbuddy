package com.mskdevelopers.helipadbuddy.domain.calculation

/**
 * Density altitude from pressure altitude and temperature.
 * DA = PA + (118.8 × (OAT - ISA_temp))
 * ISA temp at pressure altitude: ISA = 15 - 0.0065 × PA (simplified; PA in meters).
 * Or use standard: DA ≈ PA + 120 × (OAT°C - ISA°C).
 */
object DensityAltitudeCalculator {

    /** ISA lapse rate °C per meter (approx). */
    private const val ISA_LAPSE_PER_M = 0.0065f

    /** ISA sea level temp °C */
    private const val ISA_SEA_LEVEL_C = 15f

    /**
     * ISA temperature at given pressure altitude (meters).
     */
    fun isaTemperatureCelsiusAtPressureAltitude(pressureAltitudeMeters: Float): Float =
        ISA_SEA_LEVEL_C - ISA_LAPSE_PER_M * pressureAltitudeMeters

    /**
     * Density altitude in meters.
     * DA = PA + (pressureAltitudeMeters/273.15f) * (oatCelsius - isaTemp) * 100f approx.
     * Simplified: DA = PA + 120 * (OAT - ISA_Temp) when using meters; factor ~120 for feet.
     * In meters: correction ~ 0.12 * (OAT - ISA) * 1000 = 120 * (OAT - ISA) for feet, so in meters ~36.6 * (OAT - ISA).
     */
    fun densityAltitudeMeters(
        pressureAltitudeMeters: Float,
        oatCelsius: Float
    ): Float {
        val isaTemp = isaTemperatureCelsiusAtPressureAltitude(pressureAltitudeMeters)
        val correctionMeters = 100f * (oatCelsius - isaTemp) / ISA_LAPSE_PER_M
        return pressureAltitudeMeters + correctionMeters
    }

    fun densityAltitudeFeet(
        pressureAltitudeFeet: Float,
        oatCelsius: Float
    ): Float {
        val pressureAltitudeMeters = pressureAltitudeFeet / 3.28084f
        val daMeters = densityAltitudeMeters(pressureAltitudeMeters, oatCelsius)
        return daMeters * 3.28084f
    }
}
