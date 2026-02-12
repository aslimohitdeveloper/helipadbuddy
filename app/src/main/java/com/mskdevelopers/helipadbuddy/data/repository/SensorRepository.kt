package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context

/**
 * Central aggregation of sensor repositories.
 * ViewModels can use this to get all repos from one place.
 */
class SensorRepository(context: Context) {

    val locationRepository = LocationRepository(context)
    val pressureRepository = PressureRepository(context)
    val motionRepository = MotionRepository(context)
    val lightRepository = LightRepository(context)

    fun hasPressureSensor(): Boolean = pressureRepository.hasPressureSensor()
    fun hasAccelerometer(): Boolean = motionRepository.hasAccelerometer()
    fun hasGyroscope(): Boolean = motionRepository.hasGyroscope()
    fun hasMagnetometer(): Boolean = motionRepository.hasMagnetometer()
    fun hasLightSensor(): Boolean = lightRepository.hasLightSensor()
}
