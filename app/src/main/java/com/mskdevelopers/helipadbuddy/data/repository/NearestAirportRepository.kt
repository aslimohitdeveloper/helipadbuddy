package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import com.mskdevelopers.helipadbuddy.data.model.MetarStation
import com.mskdevelopers.helipadbuddy.data.remote.ApiJson
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class NearestAirportRepository(context: Context) {

    private val stations: List<MetarStation> by lazy {
        try {
            context.assets.open("airports/metar_stations.json").bufferedReader().use { reader ->
                ApiJson.instance.decodeFromString<List<MetarStation>>(reader.readText())
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun findNearest(latitude: Double, longitude: Double): MetarStation? {
        if (stations.isEmpty()) return null
        return stations.minByOrNull { haversineKm(latitude, longitude, it.latitude, it.longitude) }
    }

    fun findByIcao(icao: String): MetarStation? {
        val code = icao.trim().uppercase()
        if (code.length != 4) return null
        return stations.firstOrNull { it.icao.equals(code, ignoreCase = true) }
    }

    fun searchByPrefix(prefix: String, limit: Int = 20): List<MetarStation> {
        val q = prefix.trim().uppercase()
        if (q.isEmpty()) return emptyList()
        return stations
            .asSequence()
            .filter { it.icao.startsWith(q) || it.name.uppercase().contains(q) }
            .take(limit)
            .toList()
    }

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
        haversineKm(lat1, lon1, lat2, lon2)

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }
}
