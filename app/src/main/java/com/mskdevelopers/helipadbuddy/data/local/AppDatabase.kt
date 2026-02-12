package com.mskdevelopers.helipadbuddy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FlightSession::class, FlightDataPoint::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightSessionDao(): FlightSessionDao
    abstract fun flightDataPointDao(): FlightDataPointDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "helipad_buddy_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
