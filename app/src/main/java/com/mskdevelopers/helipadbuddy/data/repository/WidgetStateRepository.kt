package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mskdevelopers.helipadbuddy.data.model.ForecastPoint
import com.mskdevelopers.helipadbuddy.data.model.PressureWidgetData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.remote.ApiJson
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_weather")

class WidgetStateRepository(private val context: Context) {

    val weatherFlow: Flow<WidgetWeatherData> = context.widgetDataStore.data.map { prefs ->
        prefsToWidgetData(prefs)
    }

    suspend fun getCurrent(): WidgetWeatherData = weatherFlow.first()

    suspend fun save(data: WidgetWeatherData) {
        context.widgetDataStore.edit { prefs ->
            writeWidgetData(prefs, data)
        }
    }

    suspend fun setRefreshing(refreshing: Boolean) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_REFRESHING] = refreshing
            if (!refreshing) {
                prefs[KEY_REFRESH_SPIN_FRAME] = 0
            }
        }
    }

    suspend fun setRefreshSpinFrame(frame: Int) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_REFRESH_SPIN_FRAME] = frame
        }
    }

    suspend fun mergePressure(pressure: PressureWidgetData) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_QFE] = pressure.qfeHpa
            prefs[KEY_QNH_PHONE] = pressure.qnhHpa
            prefs[KEY_QFF] = pressure.qffHpa
            prefs[KEY_PRESSURE_TREND] = pressure.pressureTrend
            prefs[KEY_PRESSURE_TREND_DIR] = pressure.trendDirection.name
        }
    }

    suspend fun updateGpsSnapshot(altitudeMsl: Int, satelliteCount: Int, gpsQuality: String, lat: Double, lon: Double) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_ALT] = altitudeMsl
            prefs[KEY_SATS] = satelliteCount
            prefs[KEY_GPS] = gpsQuality
            prefs[KEY_LAT] = lat.toFloat()
            prefs[KEY_LON] = lon.toFloat()
        }
    }

    private fun prefsToWidgetData(prefs: Preferences): WidgetWeatherData = WidgetWeatherData(
        station = prefs[KEY_STATION] ?: "",
        windDirection = prefs[KEY_WIND_DIR] ?: 0,
        windSpeedKt = prefs[KEY_WIND_SPD] ?: 0,
        temperature = prefs[KEY_TEMP] ?: 0f,
        dewPoint = prefs[KEY_DEW] ?: 0f,
        humidity = prefs[KEY_HUMID] ?: 0,
        visibilityKm = prefs[KEY_VIS] ?: 0f,
        cloudCover = prefs[KEY_CLOUD] ?: 0,
        weather = prefs[KEY_WEATHER] ?: "",
        qnh = prefs[KEY_QNH] ?: 1013.25f,
        qnhMetarHpa = prefs[KEY_QNH_METAR] ?: 0f,
        qfeHpa = prefs[KEY_QFE] ?: 0f,
        refreshSpinFrame = prefs[KEY_REFRESH_SPIN_FRAME] ?: 0,
        qnhPhoneHpa = prefs[KEY_QNH_PHONE] ?: 0f,
        qffHpa = prefs[KEY_QFF] ?: 0f,
        pressureTrend = prefs[KEY_PRESSURE_TREND] ?: 0f,
        pressureTrendDirection = prefs[KEY_PRESSURE_TREND_DIR] ?: "STABLE",
        metarRaw = prefs[KEY_METAR] ?: "",
        weatherSource = prefs[KEY_WEATHER_SOURCE] ?: "Open-Meteo (GPS)",
        isRefreshing = prefs[KEY_REFRESHING] ?: false,
        altitudeMsl = prefs[KEY_ALT] ?: 0,
        satelliteCount = prefs[KEY_SATS] ?: 0,
        gpsQuality = prefs[KEY_GPS] ?: "NO_FIX",
        latitude = prefs[KEY_LAT]?.toDouble() ?: 0.0,
        longitude = prefs[KEY_LON]?.toDouble() ?: 0.0,
        activeRunwayEnd = prefs[KEY_ACTIVE_RWY] ?: "",
        headwindKt = prefs[KEY_HEADWIND] ?: 0f,
        tailwindKt = prefs[KEY_TAILWIND] ?: 0f,
        crosswindKt = prefs[KEY_CROSSWIND] ?: 0f,
        crosswindSide = prefs[KEY_CROSSWIND_SIDE] ?: "",
        runwayConfigured = prefs[KEY_RWY_CONFIGURED] ?: false,
        alertSeverity = prefs[KEY_ALERT_SEVERITY] ?: "",
        updatedAtMillis = prefs[KEY_UPDATED] ?: 0L,
        cloudLow = prefs[KEY_CLOUD_LOW] ?: "",
        cloudMedium = prefs[KEY_CLOUD_MED] ?: "",
        cloudHigh = prefs[KEY_CLOUD_HIGH] ?: "",
        ceilingFt = prefs[KEY_CEILING_FT] ?: 0,
        elevationMslMeters = prefs[KEY_ELEVATION_MSL] ?: 0,
        forecastPoints = decodeForecast(prefs[KEY_FORECAST_JSON]),
        openMeteoDiagnostics = prefs[KEY_OPEN_METEO_DIAG] ?: ""
    )

    private fun writeWidgetData(prefs: androidx.datastore.preferences.core.MutablePreferences, data: WidgetWeatherData) {
        prefs[KEY_STATION] = data.station
        prefs[KEY_WIND_DIR] = data.windDirection
        prefs[KEY_WIND_SPD] = data.windSpeedKt
        prefs[KEY_TEMP] = data.temperature
        prefs[KEY_DEW] = data.dewPoint
        prefs[KEY_HUMID] = data.humidity
        prefs[KEY_VIS] = data.visibilityKm
        prefs[KEY_CLOUD] = data.cloudCover
        prefs[KEY_WEATHER] = data.weather
        prefs[KEY_QNH] = data.qnh
        prefs[KEY_QNH_METAR] = data.qnhMetarHpa
        prefs[KEY_QFE] = data.qfeHpa
        prefs[KEY_REFRESH_SPIN_FRAME] = data.refreshSpinFrame
        prefs[KEY_QNH_PHONE] = data.qnhPhoneHpa
        prefs[KEY_QFF] = data.qffHpa
        prefs[KEY_PRESSURE_TREND] = data.pressureTrend
        prefs[KEY_PRESSURE_TREND_DIR] = data.pressureTrendDirection
        prefs[KEY_METAR] = data.metarRaw
        prefs[KEY_WEATHER_SOURCE] = data.weatherSource
        prefs[KEY_REFRESHING] = data.isRefreshing
        prefs[KEY_ALT] = data.altitudeMsl
        prefs[KEY_SATS] = data.satelliteCount
        prefs[KEY_GPS] = data.gpsQuality
        prefs[KEY_LAT] = data.latitude.toFloat()
        prefs[KEY_LON] = data.longitude.toFloat()
        prefs[KEY_ACTIVE_RWY] = data.activeRunwayEnd
        prefs[KEY_HEADWIND] = data.headwindKt
        prefs[KEY_TAILWIND] = data.tailwindKt
        prefs[KEY_CROSSWIND] = data.crosswindKt
        prefs[KEY_CROSSWIND_SIDE] = data.crosswindSide
        prefs[KEY_RWY_CONFIGURED] = data.runwayConfigured
        prefs[KEY_ALERT_SEVERITY] = data.alertSeverity
        prefs[KEY_UPDATED] = data.updatedAtMillis
        prefs[KEY_CLOUD_LOW] = data.cloudLow
        prefs[KEY_CLOUD_MED] = data.cloudMedium
        prefs[KEY_CLOUD_HIGH] = data.cloudHigh
        prefs[KEY_CEILING_FT] = data.ceilingFt
        prefs[KEY_ELEVATION_MSL] = data.elevationMslMeters
        prefs[KEY_FORECAST_JSON] = encodeForecast(data.forecastPoints)
        prefs[KEY_OPEN_METEO_DIAG] = data.openMeteoDiagnostics
    }

    private fun encodeForecast(points: List<ForecastPoint>): String =
        runCatching { ApiJson.instance.encodeToString(points) }.getOrDefault("[]")

    private fun decodeForecast(json: String?): List<ForecastPoint> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            ApiJson.instance.decodeFromString<List<ForecastPoint>>(json)
        }.getOrDefault(emptyList())
    }

    companion object {
        private val KEY_STATION = stringPreferencesKey("station")
        private val KEY_WIND_DIR = intPreferencesKey("wind_dir")
        private val KEY_WIND_SPD = intPreferencesKey("wind_spd")
        private val KEY_TEMP = floatPreferencesKey("temp")
        private val KEY_DEW = floatPreferencesKey("dew")
        private val KEY_HUMID = intPreferencesKey("humid")
        private val KEY_VIS = floatPreferencesKey("vis")
        private val KEY_CLOUD = intPreferencesKey("cloud")
        private val KEY_WEATHER = stringPreferencesKey("weather")
        private val KEY_QNH = floatPreferencesKey("qnh")
        private val KEY_QNH_METAR = floatPreferencesKey("qnh_metar_hpa")
        private val KEY_QFE = floatPreferencesKey("qfe_hpa")
        private val KEY_REFRESH_SPIN_FRAME = intPreferencesKey("refresh_spin_frame")
        private val KEY_QNH_PHONE = floatPreferencesKey("qnh_phone_hpa")
        private val KEY_QFF = floatPreferencesKey("qff_hpa")
        private val KEY_PRESSURE_TREND = floatPreferencesKey("pressure_trend")
        private val KEY_PRESSURE_TREND_DIR = stringPreferencesKey("pressure_trend_dir")
        private val KEY_METAR = stringPreferencesKey("metar")
        private val KEY_WEATHER_SOURCE = stringPreferencesKey("weather_source")
        private val KEY_REFRESHING = androidx.datastore.preferences.core.booleanPreferencesKey("refreshing")
        private val KEY_ALT = intPreferencesKey("alt_msl")
        private val KEY_SATS = intPreferencesKey("sats")
        private val KEY_GPS = stringPreferencesKey("gps_quality")
        private val KEY_LAT = floatPreferencesKey("lat")
        private val KEY_LON = floatPreferencesKey("lon")
        private val KEY_ACTIVE_RWY = stringPreferencesKey("active_rwy")
        private val KEY_HEADWIND = floatPreferencesKey("headwind_kt")
        private val KEY_TAILWIND = floatPreferencesKey("tailwind_kt")
        private val KEY_CROSSWIND = floatPreferencesKey("crosswind_kt")
        private val KEY_CROSSWIND_SIDE = stringPreferencesKey("crosswind_side")
        private val KEY_RWY_CONFIGURED = androidx.datastore.preferences.core.booleanPreferencesKey("rwy_configured")
        private val KEY_ALERT_SEVERITY = stringPreferencesKey("alert_severity")
        private val KEY_UPDATED = longPreferencesKey("updated")
        private val KEY_CLOUD_LOW = stringPreferencesKey("cloud_low")
        private val KEY_CLOUD_MED = stringPreferencesKey("cloud_med")
        private val KEY_CLOUD_HIGH = stringPreferencesKey("cloud_high")
        private val KEY_CEILING_FT = intPreferencesKey("ceiling_ft")
        private val KEY_ELEVATION_MSL = intPreferencesKey("elevation_msl")
        private val KEY_FORECAST_JSON = stringPreferencesKey("forecast_json")
        private val KEY_OPEN_METEO_DIAG = stringPreferencesKey("open_meteo_diag")
    }
}
