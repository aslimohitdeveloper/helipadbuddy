package com.mskdevelopers.helipadbuddy.domain.calculation

/**
 * Aviation pressure and altitude calculations.
 * ICAO standard atmosphere: QNH = QFE × exp(altitude / 8434) (altitude in meters, pressure in hPa).
 */
object PressureCalculations {

    /** Scale height for pressure altitude (meters). ICAO ~8434m. */
    private const val SCALE_HEIGHT_M = 8434f

    /** Standard sea-level pressure (hPa). */
    const val STANDARD_PRESSURE_HPA = 1013.25f

    /**
     * QNH from QFE and field altitude (meters).
     * QNH = QFE × exp(altitudeM / 8434)
     */
    fun qnhFromQfe(qfeHpa: Float, altitudeMeters: Float): Float {
        if (qfeHpa <= 0f) return 0f
        return (qfeHpa * kotlin.math.exp(altitudeMeters / SCALE_HEIGHT_M)).toFloat()
    }

    /**
     * Pressure altitude (meters) from pressure.
     * Alt = 8434 × ln(1013.25 / pressureHpa)
     */
    fun pressureAltitudeMeters(pressureHpa: Float): Float {
        if (pressureHpa <= 0f) return 0f
        return (SCALE_HEIGHT_M * kotlin.math.ln(STANDARD_PRESSURE_HPA / pressureHpa)).toFloat()
    }

    /**
     * Pressure altitude in feet.
     */
    fun pressureAltitudeFeet(pressureHpa: Float): Float =
        metersToFeet(pressureAltitudeMeters(pressureHpa))

    fun metersToFeet(meters: Float): Float = meters * 3.28084f
    fun feetToMeters(feet: Float): Float = feet / 3.28084f
}
