package com.mskdevelopers.helipadbuddy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FlightSession::class, FlightDataPoint::class, TerrainElevationCache::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightSessionDao(): FlightSessionDao
    abstract fun flightDataPointDao(): FlightDataPointDao
    abstract fun terrainElevationDao(): TerrainElevationDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE flight_data_points ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    "ALTER TABLE flight_data_points ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    "ALTER TABLE flight_data_points ADD COLUMN altitudeMslMeters REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    "ALTER TABLE flight_data_points ADD COLUMN altitudeWgs84Meters REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS terrain_elevation_cache (
                        gridKey TEXT NOT NULL PRIMARY KEY,
                        elevationMeters REAL NOT NULL,
                        fetchedAtMillis INTEGER NOT NULL
                    )"""
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "helipad_buddy_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
