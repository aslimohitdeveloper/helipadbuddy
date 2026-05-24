package com.mskdevelopers.helipadbuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TerrainElevationDao {
    @Query("SELECT * FROM terrain_elevation_cache WHERE gridKey = :key LIMIT 1")
    suspend fun getByKey(key: String): TerrainElevationCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TerrainElevationCache)
}
