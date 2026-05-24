package com.mskdevelopers.helipadbuddy.domain.calculation

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

object WeatherDerivedValues {

    data class ThermoSnapshot(
        val temperatureC: Float,
        val dewPointC: Float,
        val humidityPercent: Int
    )

    fun normalizeThermodynamics(temperatureC: Float, dewPointC: Float, humidityPercent: Int): ThermoSnapshot {
        var temp = temperatureC
        var dew = dewPointC
        var rh = humidityPercent.coerceIn(0, 100)
        if (temp != 0f && dew > temp) {
            dew = temp
        }
        if (temp != 0f && dew != 0f) {
            rh = relativeHumidityFromDewPoint(temp, dew)
        } else if (temp != 0f && rh > 0) {
            dew = dewPointFromRelativeHumidity(temp, rh)
            if (dew > temp) dew = temp
        }
        return ThermoSnapshot(temp, dew, rh)
    }

    fun relativeHumidityFromDewPoint(tempC: Float, dewC: Float): Int {
        if (tempC == 0f) return 0
        val a = 17.27
        val b = 237.7
        val es = 6.112 * exp((a * tempC) / (b + tempC))
        val e = 6.112 * exp((a * dewC) / (b + dewC))
        if (es <= 0.0) return 0
        return ((e / es) * 100.0).toInt().coerceIn(0, 100)
    }

    fun dewPointFromRelativeHumidity(tempC: Float, humidityPercent: Int): Float {
        if (tempC == 0f || humidityPercent <= 0) return tempC - 5f
        val a = 17.27
        val b = 237.7
        val rh = humidityPercent.coerceIn(1, 100) / 100.0
        val alpha = a * tempC / (b + tempC) + ln(rh)
        return ((b * alpha) / (a - alpha)).toFloat()
    }

    /** Saturation vapor pressure eₛ (hPa) at air temperature. */
    fun saturationVaporPressureHpa(tempC: Float): Float {
        if (tempC == 0f) return 0f
        val a = 17.27
        val b = 237.7
        return (6.112 * exp((a * tempC) / (b + tempC))).toFloat()
    }

    /** Actual vapor pressure e (hPa) from temperature and relative humidity. */
    fun vaporPressureHpa(tempC: Float, humidityPercent: Int): Float {
        if (tempC == 0f || humidityPercent <= 0) return 0f
        val es = saturationVaporPressureHpa(tempC)
        return es * (humidityPercent.coerceIn(0, 100) / 100f)
    }

    fun heatIndexC(tempC: Float, humidityPercent: Int): Float {
        if (tempC < 27f) return tempC
        val rh = humidityPercent.coerceIn(0, 100).toFloat()
        val tempF = tempC * 9f / 5f + 32f
        val hiF = -42.379f +
            2.04901523f * tempF +
            10.14333127f * rh -
            0.22475541f * tempF * rh -
            0.00683783f * tempF.pow(2) -
            0.05481717f * rh.pow(2) +
            0.00122874f * tempF.pow(2) * rh +
            0.00085282f * tempF * rh.pow(2) -
            0.00000199f * tempF.pow(2) * rh.pow(2)
        return (hiF - 32f) * 5f / 9f
    }

    /** Stull (2011) wet-bulb approximation for °C. */
    fun wetBulbC(tempC: Float, dewC: Float): Float {
        if (tempC == 0f) return 0f
        val rh = relativeHumidityFromDewPoint(tempC, dewC.coerceAtMost(tempC)).toFloat()
        return (tempC * kotlin.math.atan(0.151977 * kotlin.math.sqrt(rh + 8.313659)) +
            kotlin.math.atan(tempC + rh) -
            kotlin.math.atan(rh - 1.676331) +
            0.00391838 * rh.pow(1.5f) * kotlin.math.atan(0.023101 * rh) -
            4.686035).toFloat()
    }

    fun densityAltitudeFeet(pressureAltFeet: Float, tempC: Float): Float {
        val isaTempC = 15f - (pressureAltFeet / 1000f) * 2f
        return pressureAltFeet + 120f * (tempC - isaTempC)
    }
}
