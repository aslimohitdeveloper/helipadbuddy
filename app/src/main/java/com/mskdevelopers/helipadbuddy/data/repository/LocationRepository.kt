package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * GNSS and fused location data.
 * Exposes position (WGS84) and GNSS health via Flow.
 */
class LocationRepository(private val context: Context) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateIntervalMillis(500L)
        .setMaxUpdates(10000)
        .setWaitForAccurateLocation(false)
        .build()

    /** Position from FusedLocationProviderClient (track = course over ground). Heading from magnetometer; ViewModel merges. */
    fun positionFlow(): Flow<PositionData> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(PositionData.EMPTY)
            close()
            return@callbackFlow
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val altFt = AviationFormulas.metersToFeet(location.altitude.toFloat())
                val speedKnots = AviationFormulas.mpsToKnots(location.speed)
                val speedKmh = AviationFormulas.mpsToKmh(location.speed)
                val track = if (location.hasBearing()) location.bearing else 0f
                trySend(
                    PositionData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitudeMeters = location.altitude,
                        altitudeFeet = altFt.toDouble(),
                        groundSpeedMps = location.speed,
                        groundSpeedKnots = speedKnots,
                        groundSpeedKmh = speedKmh,
                        trackDegrees = track,
                        headingDegrees = 0f,
                        trackVsHeadingDegrees = 0f,
                        hasFix = true,
                        timestamp = location.time
                    )
                )
            }
        }
        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            trySend(PositionData.EMPTY)
        }
        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    /** Single-shot last location (e.g. for initial QNH with altitude). */
    suspend fun getLastLocation(): Location? = try {
        if (!hasLocationPermission()) null
        else fusedClient.lastLocation.await()
    } catch (e: SecurityException) {
        null
    } catch (e: Exception) {
        null
    }

    private fun hasLocationPermission(): Boolean {
        return try {
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    /** Whether location (GPS) is enabled in system settings. */
    fun isLocationEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    /** Emits whether location is enabled, periodically, so UI updates when user toggles GPS. */
    fun locationEnabledFlow(): Flow<Boolean> = flow {
        while (true) {
            emit(isLocationEnabled())
            delay(2000)
        }
    }

    /** GNSS status: satellites in view, used, constellations, SNR. */
    fun gnssStatusFlow(): Flow<GnssHealthData> = callbackFlow {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            trySend(GnssHealthData.EMPTY)
            close()
            return@callbackFlow
        }
        if (!hasLocationPermission()) {
            trySend(GnssHealthData.EMPTY)
            close()
            return@callbackFlow
        }
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val inView = status.satelliteCount
                var used = 0
                var sumCn0 = 0f
                var countCn0 = 0
                var gps = 0
                var galileo = 0
                var glonass = 0
                var beidou = 0
                for (i in 0 until status.satelliteCount) {
                    if (status.usedInFix(i)) used++
                    val cn0 = status.getCn0DbHz(i)
                    if (cn0 > 0f) {
                        sumCn0 += cn0
                        countCn0++
                    }
                    when (status.getConstellationType(i)) {
                        GnssStatus.CONSTELLATION_GPS -> gps++
                        GnssStatus.CONSTELLATION_GALILEO -> galileo++
                        GnssStatus.CONSTELLATION_GLONASS -> glonass++
                        GnssStatus.CONSTELLATION_BEIDOU -> beidou++
                        else -> {}
                    }
                }
                val avgSnr = if (countCn0 > 0) sumCn0 / countCn0 else 0f
                val quality = when {
                    used >= 6 && avgSnr >= 30f -> GnssHealthData.GnssQuality.GOOD
                    used >= 4 && avgSnr >= 20f -> GnssHealthData.GnssQuality.MARGINAL
                    used > 0 -> GnssHealthData.GnssQuality.POOR
                    else -> GnssHealthData.GnssQuality.NO_FIX
                }
                trySend(
                    GnssHealthData(
                        satellitesInView = inView,
                        satellitesUsedInFix = used,
                        gpsCount = gps,
                        galileoCount = galileo,
                        glonassCount = glonass,
                        beidouCount = beidou,
                        averageSnrDbHz = avgSnr,
                        quality = quality,
                        hasFix = used > 0
                    )
                )
            }
        }
        try {
            locationManager.registerGnssStatusCallback(callback)
        } catch (e: SecurityException) {
            trySend(GnssHealthData.EMPTY)
        }
        awaitClose {
            locationManager.unregisterGnssStatusCallback(callback)
        }
    }
}
