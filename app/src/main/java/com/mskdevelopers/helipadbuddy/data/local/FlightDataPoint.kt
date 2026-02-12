package com.mskdevelopers.helipadbuddy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Single data point within a flight session.
 * Logs altitude, speed, heading, VSI, G-load at a timestamp.
 */
@Entity(
    tableName = "flight_data_points",
    foreignKeys = [
        ForeignKey(
            entity = FlightSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class FlightDataPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestampMillis: Long,
    val altitudeMeters: Double,
    val groundSpeedKnots: Float,
    val headingDegrees: Float,
    val verticalSpeedFtMin: Float,
    val gLoad: Float
)
