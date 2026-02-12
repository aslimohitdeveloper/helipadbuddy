package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists user preferences (units, etc.).
 * Plan Phase 4.4: Unit preferences (feet/meters, knots/km/h).
 */
class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _unitPreferences = MutableStateFlow(loadUnitPreferences())
    val unitPreferences: StateFlow<UnitPreferences> = _unitPreferences.asStateFlow()

    fun setAltitudeFeet(feet: Boolean) {
        prefs.edit().putBoolean(KEY_ALTITUDE_FEET, feet).apply()
        _unitPreferences.value = loadUnitPreferences()
    }

    fun setSpeedKnots(knots: Boolean) {
        prefs.edit().putBoolean(KEY_SPEED_KNOTS, knots).apply()
        _unitPreferences.value = loadUnitPreferences()
    }

    /** User-input field elevation (meters) for QNH calculation. 0 means use GPS altitude. */
    fun setFieldElevationMeters(elevationMeters: Float) {
        prefs.edit().putFloat(KEY_FIELD_ELEVATION_METERS, elevationMeters).apply()
        _fieldElevationMeters.value = elevationMeters
    }

    private val _fieldElevationMeters = MutableStateFlow(prefs.getFloat(KEY_FIELD_ELEVATION_METERS, 0f))
    val fieldElevationMeters: StateFlow<Float> = _fieldElevationMeters.asStateFlow()

    private fun loadUnitPreferences(): UnitPreferences = UnitPreferences(
        altitudeFeet = prefs.getBoolean(KEY_ALTITUDE_FEET, true),
        speedKnots = prefs.getBoolean(KEY_SPEED_KNOTS, true)
    )

    companion object {
        private const val PREFS_NAME = "helipad_buddy_prefs"
        private const val KEY_ALTITUDE_FEET = "altitude_feet"
        private const val KEY_SPEED_KNOTS = "speed_knots"
        private const val KEY_FIELD_ELEVATION_METERS = "field_elevation_meters"
    }
}
