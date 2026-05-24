package com.mskdevelopers.helipadbuddy.domain.calculation

object TerrainCalculator {
    fun aglMeters(mslMeters: Double, terrainElevationMeters: Double): Double =
        mslMeters - terrainElevationMeters

    fun aglFeet(mslFeet: Double, terrainElevationFeet: Double): Double =
        mslFeet - terrainElevationFeet
}
