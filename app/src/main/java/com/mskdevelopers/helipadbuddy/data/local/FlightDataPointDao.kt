package com.mskdevelopers.helipadbuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDataPointDao {
    @Insert
    suspend fun insert(point: FlightDataPoint): Long

    @Insert
    suspend fun insertAll(points: List<FlightDataPoint>): List<Long>

    @Query("SELECT * FROM flight_data_points WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    fun getPointsForSession(sessionId: Long): Flow<List<FlightDataPoint>>

    @Query("SELECT * FROM flight_data_points WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    suspend fun getPointsForSessionOnce(sessionId: Long): List<FlightDataPoint>
}
