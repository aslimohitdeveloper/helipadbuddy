package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mskdevelopers.helipadbuddy.data.model.RunwayConfig
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import com.mskdevelopers.helipadbuddy.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow

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

    val backgroundMonitoring = preferencesRepository.backgroundMonitoring

    val runwayConfigs = preferencesRepository.runwayConfigs

    val activeRunway = preferencesRepository.activeRunway

    val preferredMetarIcao = preferencesRepository.preferredMetarIcao

    fun setPreferredMetarIcao(icao: String?) {
        preferencesRepository.setPreferredMetarIcao(icao)
    }

    fun setFieldElevationMeters(elevationMeters: Float) {
        preferencesRepository.setFieldElevationMeters(elevationMeters)
    }

    fun setBackgroundMonitoring(enabled: Boolean) {
        preferencesRepository.setBackgroundMonitoring(enabled)
    }

    fun addRunway(
        identifier: String,
        activeEnd: String,
        runwayLengthMeters: Int? = null,
        notes: String? = null
    ) {
        preferencesRepository.addRunway(identifier, activeEnd, runwayLengthMeters, notes)
    }

    fun updateRunway(config: RunwayConfig) {
        preferencesRepository.updateRunway(config)
    }

    fun setActiveRunway(runwayName: String) {
        preferencesRepository.setActiveRunway(runwayName)
    }

    fun setActiveRunwayEnd(runwayName: String, activeEnd: String) {
        preferencesRepository.setActiveRunwayEnd(runwayName, activeEnd)
    }

    fun removeRunway(runwayName: String) {
        preferencesRepository.removeRunway(runwayName)
    }
}
