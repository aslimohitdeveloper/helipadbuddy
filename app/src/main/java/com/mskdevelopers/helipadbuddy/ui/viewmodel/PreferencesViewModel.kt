package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import com.mskdevelopers.helipadbuddy.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Exposes unit preferences for Settings and MainScreen.
 * Plan Phase 4.4: Unit preferences (feet/meters, knots/km/h).
 */
class PreferencesViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val unitPreferences: StateFlow<UnitPreferences> = preferencesRepository.unitPreferences

    fun setAltitudeFeet(feet: Boolean) {
        preferencesRepository.setAltitudeFeet(feet)
    }

    fun setSpeedKnots(knots: Boolean) {
        preferencesRepository.setSpeedKnots(knots)
    }

    val fieldElevationMeters = preferencesRepository.fieldElevationMeters

    fun setFieldElevationMeters(elevationMeters: Float) {
        preferencesRepository.setFieldElevationMeters(elevationMeters)
    }
}
