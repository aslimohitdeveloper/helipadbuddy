package com.mskdevelopers.helipadbuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightSessionDao {
    @Insert
    suspend fun insert(session: FlightSession): Long

    @Update
    suspend fun update(session: FlightSession): Int

    @Query("SELECT * FROM flight_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<FlightSession>>

    @Query("SELECT * FROM flight_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): Flow<FlightSession?>

    @Query("SELECT * FROM flight_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): FlightSession?
}
