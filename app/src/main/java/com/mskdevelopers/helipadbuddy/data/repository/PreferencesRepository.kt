package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mskdevelopers.helipadbuddy.data.model.RunwayConfig
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import com.mskdevelopers.helipadbuddy.data.remote.ApiJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

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

    fun setFieldElevationMeters(elevationMeters: Float) {
        prefs.edit().putFloat(KEY_FIELD_ELEVATION_METERS, elevationMeters).apply()
        _fieldElevationMeters.value = elevationMeters
    }

    private val _fieldElevationMeters = MutableStateFlow(prefs.getFloat(KEY_FIELD_ELEVATION_METERS, 0f))
    val fieldElevationMeters: StateFlow<Float> = _fieldElevationMeters.asStateFlow()

    private val _backgroundMonitoring = MutableStateFlow(prefs.getBoolean(KEY_BACKGROUND_MONITORING, false))
    val backgroundMonitoring: StateFlow<Boolean> = _backgroundMonitoring.asStateFlow()

    fun setBackgroundMonitoring(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKGROUND_MONITORING, enabled).apply()
        _backgroundMonitoring.value = enabled
    }

    private val _runwayConfigs = MutableStateFlow(loadRunwayConfigs())
    val runwayConfigs: StateFlow<List<RunwayConfig>> = _runwayConfigs.asStateFlow()

    private val _activeRunway = MutableStateFlow(loadActiveRunway())
    val activeRunway: StateFlow<RunwayConfig?> = _activeRunway.asStateFlow()

    private val _preferredMetarIcao = MutableStateFlow(loadPreferredMetarIcao())
    val preferredMetarIcao: StateFlow<String?> = _preferredMetarIcao.asStateFlow()

    fun setPreferredMetarIcao(icao: String?) {
        val normalized = icao
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.length == 4 && it.all { ch -> ch.isLetter() } }
        if (normalized == null) {
            prefs.edit().remove(KEY_PREFERRED_METAR_ICAO).apply()
        } else {
            prefs.edit().putString(KEY_PREFERRED_METAR_ICAO, normalized).apply()
        }
        _preferredMetarIcao.value = normalized
    }

    fun addRunway(
        identifier: String,
        activeEnd: String,
        runwayLengthMeters: Int? = null,
        notes: String? = null
    ) {
        val config = RunwayConfig.fromIdentifier(identifier, activeEnd, runwayLengthMeters, notes) ?: return
        val updated = _runwayConfigs.value
            .filterNot { it.runwayName.equals(config.runwayName, ignoreCase = true) }
            .plus(config)
        saveRunwayConfigs(updated)
        if (_activeRunway.value == null) {
            setActiveRunway(config.runwayName)
        }
    }

    fun updateRunway(config: RunwayConfig) {
        val updated = _runwayConfigs.value.map {
            if (it.runwayName.equals(config.runwayName, ignoreCase = true)) config else it
        }
        saveRunwayConfigs(updated)
        if (_activeRunway.value?.runwayName.equals(config.runwayName, ignoreCase = true)) {
            _activeRunway.value = config
            prefs.edit().putString(KEY_ACTIVE_RUNWAY, config.runwayName).apply()
        }
    }

    fun addRunway(identifier: String, activeEnd: String) {
        addRunway(identifier, activeEnd, null, null)
    }

    fun setActiveRunway(runwayName: String) {
        val config = _runwayConfigs.value.firstOrNull {
            it.runwayName.equals(runwayName, ignoreCase = true)
        } ?: return
        prefs.edit().putString(KEY_ACTIVE_RUNWAY, config.runwayName).apply()
        _activeRunway.value = config
    }

    fun setActiveRunwayEnd(runwayName: String, activeEnd: String) {
        val existing = _runwayConfigs.value.firstOrNull {
            it.runwayName.equals(runwayName, ignoreCase = true)
        } ?: return
        val updatedConfig = existing.copy(activeRunway = activeEnd)
        val updatedList = _runwayConfigs.value.map {
            if (it.runwayName.equals(runwayName, ignoreCase = true)) updatedConfig else it
        }
        saveRunwayConfigs(updatedList)
        if (_activeRunway.value?.runwayName.equals(runwayName, ignoreCase = true)) {
            _activeRunway.value = updatedConfig
        }
    }

    fun removeRunway(runwayName: String) {
        val updated = _runwayConfigs.value.filterNot {
            it.runwayName.equals(runwayName, ignoreCase = true)
        }
        saveRunwayConfigs(updated)
        if (_activeRunway.value?.runwayName.equals(runwayName, ignoreCase = true)) {
            val next = updated.firstOrNull()
            if (next != null) {
                setActiveRunway(next.runwayName)
            } else {
                prefs.edit().remove(KEY_ACTIVE_RUNWAY).apply()
                _activeRunway.value = null
            }
        }
    }

    private fun saveRunwayConfigs(configs: List<RunwayConfig>) {
        val json = ApiJson.instance.encodeToString(ListSerializer(RunwayConfig.serializer()), configs)
        prefs.edit().putString(KEY_RUNWAYS, json).apply()
        _runwayConfigs.value = configs
    }

    private fun loadRunwayConfigs(): List<RunwayConfig> {
        val json = prefs.getString(KEY_RUNWAYS, null) ?: return emptyList()
        return try {
            ApiJson.instance.decodeFromString(ListSerializer(RunwayConfig.serializer()), json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadActiveRunway(): RunwayConfig? {
        val name = prefs.getString(KEY_ACTIVE_RUNWAY, null) ?: return null
        return loadRunwayConfigs().firstOrNull { it.runwayName.equals(name, ignoreCase = true) }
    }

    private fun loadPreferredMetarIcao(): String? =
        prefs.getString(KEY_PREFERRED_METAR_ICAO, null)
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.length == 4 && it.all { ch -> ch.isLetter() } }

    private fun loadUnitPreferences(): UnitPreferences = UnitPreferences(
        altitudeFeet = prefs.getBoolean(KEY_ALTITUDE_FEET, true),
        speedKnots = prefs.getBoolean(KEY_SPEED_KNOTS, true)
    )

    companion object {
        private const val PREFS_NAME = "helipad_buddy_prefs"
        private const val KEY_ALTITUDE_FEET = "altitude_feet"
        private const val KEY_SPEED_KNOTS = "speed_knots"
        private const val KEY_FIELD_ELEVATION_METERS = "field_elevation_meters"
        private const val KEY_BACKGROUND_MONITORING = "background_monitoring"
        private const val KEY_RUNWAYS = "runway_configs"
        private const val KEY_ACTIVE_RUNWAY = "active_runway"
        private const val KEY_PREFERRED_METAR_ICAO = "preferred_metar_icao"
    }
}
