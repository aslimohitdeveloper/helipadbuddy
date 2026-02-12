package com.mskdevelopers.helipadbuddy.data.model

/**
 * User preferences for display units.
 * Plan Phase 4.4: Unit preferences (feet/meters, knots/km/h).
 */
data class UnitPreferences(
    val altitudeFeet: Boolean = true,  // true = feet, false = meters
    val speedKnots: Boolean = true    // true = knots, false = km/h
)
