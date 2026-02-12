package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Light sensor for auto night mode.
 * Exposes ambient light in lux.
 */
class LightRepository(private val context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    companion object {
        /** Default threshold below which we consider "night" (lux). */
        const val DEFAULT_NIGHT_THRESHOLD_LUX = 10f
    }

    fun lightFlow(): Flow<Float> = callbackFlow {
        val sensor = lightSensor
        if (sensor == null) {
            trySend(Float.MAX_VALUE) // no sensor -> treat as day
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT && event.values.isNotEmpty()) {
                    trySend(event.values[0])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun hasLightSensor(): Boolean = lightSensor != null
}
