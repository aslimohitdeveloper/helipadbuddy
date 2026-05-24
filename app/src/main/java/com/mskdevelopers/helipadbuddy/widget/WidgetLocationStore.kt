package com.mskdevelopers.helipadbuddy.widget

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

data class WidgetLocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val altitudeMsl: Int,
    val satelliteCount: Int,
    val gpsQuality: String
)

private val Context.widgetLocationStore by preferencesDataStore("widget_location")

object WidgetLocationStore {
    private val KEY_LAT = doublePreferencesKey("lat")
    private val KEY_LON = doublePreferencesKey("lon")
    private val KEY_ALT = intPreferencesKey("alt_msl")
    private val KEY_SATS = intPreferencesKey("sats")
    private val KEY_GPS = stringPreferencesKey("gps")

    suspend fun save(context: Context, snapshot: WidgetLocationSnapshot) {
        context.widgetLocationStore.edit { prefs ->
            prefs[KEY_LAT] = snapshot.latitude
            prefs[KEY_LON] = snapshot.longitude
            prefs[KEY_ALT] = snapshot.altitudeMsl
            prefs[KEY_SATS] = snapshot.satelliteCount
            prefs[KEY_GPS] = snapshot.gpsQuality
        }
    }

    suspend fun getLastLocation(context: Context): WidgetLocationSnapshot? {
        val prefs = context.widgetLocationStore.data.first()
        val lat = prefs[KEY_LAT] ?: return null
        val lon = prefs[KEY_LON] ?: return null
        return WidgetLocationSnapshot(
            latitude = lat,
            longitude = lon,
            altitudeMsl = prefs[KEY_ALT] ?: 0,
            satelliteCount = prefs[KEY_SATS] ?: 0,
            gpsQuality = prefs[KEY_GPS] ?: "NO_FIX"
        )
    }
}
