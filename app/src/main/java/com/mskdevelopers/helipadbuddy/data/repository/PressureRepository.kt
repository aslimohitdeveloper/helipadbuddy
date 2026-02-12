package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Barometer (pressure) sensor.
 * Exposes raw pressure in hPa (QFE at device).
 */
class PressureRepository(private val context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _pressureHpa = MutableStateFlow(0f)
    val pressureHpa: StateFlow<Float> = _pressureHpa

    /** Flow of pressure in hPa (QFE). */
    fun pressureFlow(): Flow<Float> = callbackFlow {
        val sensor = pressureSensor
        if (sensor == null) {
            trySend(0f)
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PRESSURE && event.values.isNotEmpty()) {
                    val hpa = event.values[0]
                    _pressureHpa.value = hpa
                    trySend(hpa)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun hasPressureSensor(): Boolean = pressureSensor != null
}
