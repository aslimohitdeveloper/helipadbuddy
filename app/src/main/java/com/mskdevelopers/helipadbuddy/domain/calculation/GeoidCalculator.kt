package com.mskdevelopers.helipadbuddy.domain.calculation

import android.content.Context
import android.location.Location
import android.os.Build
import android.location.altitude.AltitudeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor

/**
 * Resolves Mean Sea Level altitude from WGS84 ellipsoid altitude.
 * API 34+: prefers [Location.getMslAltitudeMeters] or [AltitudeConverter].
 * Fallback: EGM96 5° grid bilinear interpolation from assets.
 */
class GeoidCalculator(private val context: Context) {

    private val converterReady = AtomicBoolean(false)
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var grid: ShortArray? = null
    private val gridLatMin = -90.0
    private val gridLonMin = -180.0
    private val gridStep = 5.0
    private val gridLatCount = 37
    private val gridLonCount = 73

    init {
        loadGrid()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            initScope.launch {
                try {
                    val converter = AltitudeConverter()
                    val probe = Location("probe").apply {
                        latitude = 0.0
                        longitude = 0.0
                        altitude = 0.0
                    }
                    converter.addMslAltitudeToLocation(context, probe)
                    converterReady.set(true)
                } catch (_: Exception) {
                    converterReady.set(false)
                }
            }
        }
    }

    /** WGS84 ellipsoid altitude and MSL altitude in meters. */
    fun resolveAltitudes(location: Location): AltitudePair {
        val wgs84 = location.altitude
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (location.hasMslAltitude()) {
                return AltitudePair(wgs84, location.mslAltitudeMeters)
            }
            val copy = Location(location)
            if (converterReady.get()) {
                try {
                    if (AltitudeConverter().tryAddMslAltitudeToLocation(copy) && copy.hasMslAltitude()) {
                        return AltitudePair(wgs84, copy.mslAltitudeMeters)
                    }
                } catch (_: Exception) { }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    val converter = AltitudeConverter()
                    converter.addMslAltitudeToLocation(context, copy)
                    if (copy.hasMslAltitude()) {
                        converterReady.set(true)
                        return AltitudePair(wgs84, copy.mslAltitudeMeters)
                    }
                } catch (_: Exception) { }
            }
        }
        val offset = geoidOffsetMeters(location.latitude, location.longitude)
        return AltitudePair(wgs84, wgs84 + offset)
    }

    fun geoidOffsetMeters(latitude: Double, longitude: Double): Double {
        val g = grid ?: return approximateGeoidOffset(latitude, longitude)
        val lat = latitude.coerceIn(-90.0, 90.0)
        val lon = normalizeLon(longitude)
        val latIdx = ((lat - gridLatMin) / gridStep).coerceIn(0.0, (gridLatCount - 2).toDouble())
        val lonIdx = ((lon - gridLonMin) / gridStep).coerceIn(0.0, (gridLonCount - 2).toDouble())
        val lat0 = floor(latIdx).toInt()
        val lon0 = floor(lonIdx).toInt()
        val latF = latIdx - lat0
        val lonF = lonIdx - lon0
        val v00 = gridValue(g, lat0, lon0)
        val v01 = gridValue(g, lat0, lon0 + 1)
        val v10 = gridValue(g, lat0 + 1, lon0)
        val v11 = gridValue(g, lat0 + 1, lon0 + 1)
        val v0 = v00 * (1 - lonF) + v01 * lonF
        val v1 = v10 * (1 - lonF) + v11 * lonF
        return v0 * (1 - latF) + v1 * latF
    }

    private fun gridValue(grid: ShortArray, latIdx: Int, lonIdx: Int): Double {
        val idx = latIdx * gridLonCount + (lonIdx % gridLonCount)
        return grid[idx] / 10.0
    }

    private fun loadGrid() {
        try {
            context.assets.open("geoid/egm96_5deg.bin").use { stream ->
                val input = DataInputStream(stream)
                val count = input.readInt()
                val data = ShortArray(count)
                for (i in 0 until count) {
                    data[i] = input.readShort()
                }
                grid = data
            }
        } catch (_: Exception) {
            grid = null
        }
    }

  private fun normalizeLon(lon: Double): Double {
        var l = lon
        while (l < -180.0) l += 360.0
        while (l > 180.0) l -= 360.0
        return l
    }

    /** Spherical harmonic fallback when grid asset unavailable. */
    private fun approximateGeoidOffset(latitude: Double, longitude: Double): Double {
        val phi = Math.toRadians(latitude)
        val lam = Math.toRadians(longitude)
        return 18.0 * kotlin.math.sin(phi) +
            -6.0 * kotlin.math.cos(2.0 * phi) * kotlin.math.cos(lam) +
            12.0 * kotlin.math.sin(2.0 * phi) * kotlin.math.sin(lam) +
            -4.0 * kotlin.math.sin(3.0 * phi) +
            8.0 * kotlin.math.cos(phi) * kotlin.math.cos(2.0 * lam) +
            5.0 * kotlin.math.sin(phi) * kotlin.math.sin(2.0 * lam)
    }

    data class AltitudePair(val wgs84Meters: Double, val mslMeters: Double)
}
