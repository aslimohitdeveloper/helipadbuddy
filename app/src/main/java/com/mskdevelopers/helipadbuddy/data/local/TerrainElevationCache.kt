package com.mskdevelopers.helipadbuddy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terrain_elevation_cache")
data class TerrainElevationCache(
    @PrimaryKey
    val gridKey: String,
    val elevationMeters: Double,
    val fetchedAtMillis: Long
)
