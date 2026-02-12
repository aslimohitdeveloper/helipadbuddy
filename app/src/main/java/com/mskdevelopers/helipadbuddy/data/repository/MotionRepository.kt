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
 * Accelerometer, gyroscope, and magnetometer.
 * Exposes raw sensor values for attitude and motion ViewModels.
 */
class MotionRepository(private val context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    data class Acceleration(val x: Float, val y: Float, val z: Float)
    data class Gyro(val x: Float, val y: Float, val z: Float)  // rad/s
    data class Magnetic(val x: Float, val y: Float, val z: Float)

    fun accelerationFlow(): Flow<Acceleration> = callbackFlow {
        if (accel == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && event.values.size >= 3) {
                    trySend(
                        Acceleration(
                            event.values[0],
                            event.values[1],
                            event.values[2]
                        )
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    fun gyroscopeFlow(): Flow<Gyro> = callbackFlow {
        if (gyro == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE && event.values.size >= 3) {
                    trySend(
                        Gyro(
                            event.values[0],
                            event.values[1],
                            event.values[2]
                        )
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    fun magneticFlow(): Flow<Magnetic> = callbackFlow {
        if (mag == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD && event.values.size >= 3) {
                    trySend(
                        Magnetic(
                            event.values[0],
                            event.values[1],
                            event.values[2]
                        )
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, mag, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    fun hasAccelerometer(): Boolean = accel != null
    fun hasGyroscope(): Boolean = gyro != null
    fun hasMagnetometer(): Boolean = mag != null
}
