package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import com.mskdevelopers.helipadbuddy.data.local.AppDatabase
import com.mskdevelopers.helipadbuddy.data.local.FlightDataPoint
import com.mskdevelopers.helipadbuddy.data.local.FlightSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileWriter

/**
 * Flight session logging: start/stop session, record points, export CSV.
 */
class LoggingRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val sessionDao = db.flightSessionDao()
    private val pointDao = db.flightDataPointDao()

    fun getAllSessions(): Flow<List<FlightSession>> = sessionDao.getAllSessions()
    fun getActiveSession(): Flow<FlightSession?> = sessionDao.getActiveSession()

    suspend fun startSession(): Long {
        return withContext(Dispatchers.IO) {
            sessionDao.insert(
                FlightSession(
                    startTimeMillis = System.currentTimeMillis(),
                    isActive = true
                )
            )
        }
    }

    suspend fun stopSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            sessionDao.getSessionById(sessionId)?.let { session ->
                sessionDao.update(
                    session.copy(endTimeMillis = System.currentTimeMillis(), isActive = false)
                )
            }
        }
    }

    suspend fun addDataPoint(
        sessionId: Long,
        altitudeMeters: Double,
        groundSpeedKnots: Float,
        headingDegrees: Float,
        verticalSpeedFtMin: Float,
        gLoad: Float
    ) {
        withContext(Dispatchers.IO) {
            pointDao.insert(
                FlightDataPoint(
                    sessionId = sessionId,
                    timestampMillis = System.currentTimeMillis(),
                    altitudeMeters = altitudeMeters,
                    groundSpeedKnots = groundSpeedKnots,
                    headingDegrees = headingDegrees,
                    verticalSpeedFtMin = verticalSpeedFtMin,
                    gLoad = gLoad
                )
            )
        }
    }

    /**
     * Export session to CSV file. Returns the file path or null on error.
     */
    suspend fun exportSessionToCsv(sessionId: Long, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val points = pointDao.getPointsForSessionOnce(sessionId)
            FileWriter(outputFile).use { writer ->
                writer.write("timestamp_ms,altitude_m,ground_speed_kt,heading_deg,vsi_ft_min,g_load\n")
                points.forEach { p ->
                    writer.write("${p.timestampMillis},${p.altitudeMeters},${p.groundSpeedKnots},${p.headingDegrees},${p.verticalSpeedFtMin},${p.gLoad}\n")
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
