package com.mskdevelopers.helipadbuddy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Flight session entity for logging.
 * One session contains many FlightDataPoints.
 */
@Entity(tableName = "flight_sessions")
data class FlightSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    var endTimeMillis: Long? = null,
    val isActive: Boolean = true
)
