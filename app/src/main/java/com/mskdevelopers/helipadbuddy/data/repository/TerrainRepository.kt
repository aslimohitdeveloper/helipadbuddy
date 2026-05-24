package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import com.mskdevelopers.helipadbuddy.data.local.AppDatabase
import com.mskdevelopers.helipadbuddy.data.local.TerrainElevationCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TerrainRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).terrainElevationDao()
    private val weatherRepo = WeatherWidgetRepository()
    private val cacheValidMs = TimeUnit.DAYS.toMillis(30)

    suspend fun getTerrainElevationMeters(latitude: Double, longitude: Double): Double? =
        withContext(Dispatchers.IO) {
            val key = gridKey(latitude, longitude)
            val cached = dao.getByKey(key)
            if (cached != null && System.currentTimeMillis() - cached.fetchedAtMillis < cacheValidMs) {
                return@withContext cached.elevationMeters
            }
            val elevation = weatherRepo.fetchElevationMeters(latitude, longitude) ?: return@withContext null
            dao.insert(
                TerrainElevationCache(
                    gridKey = key,
                    elevationMeters = elevation,
                    fetchedAtMillis = System.currentTimeMillis()
                )
            )
            elevation
        }

    private fun gridKey(lat: Double, lon: Double): String {
        val latR = (kotlin.math.round(lat * 100) / 100.0)
        val lonR = (kotlin.math.round(lon * 100) / 100.0)
        return "${latR}_${lonR}"
    }
}
