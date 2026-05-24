package com.mskdevelopers.helipadbuddy.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ApiJson {
    val instance = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}

@Serializable
data class OpenMeteoResponse(
    val current: OpenMeteoCurrent? = null,
    val hourly: OpenMeteoHourly? = null
)

@Serializable
data class OpenMeteoHourly(
    val time: List<String>? = null,
    @SerialName("temperature_2m") val temperature2m: List<Double>? = null,
    @SerialName("wind_speed_10m") val windSpeed10m: List<Double>? = null,
    @SerialName("wind_direction_10m") val windDirection10m: List<Int>? = null,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int>? = null,
    @SerialName("cloud_cover") val cloudCover: List<Int>? = null
)

@Serializable
data class OpenMeteoCurrent(
    @SerialName("temperature_2m") val temperature2m: Double? = null,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @SerialName("dewpoint_2m") val dewpoint2m: Double? = null,
    @SerialName("wind_speed_10m") val windSpeed10m: Double? = null,
    @SerialName("wind_direction_10m") val windDirection10m: Int? = null,
    @SerialName("cloud_cover") val cloudCover: Int? = null,
    @SerialName("visibility") val visibility: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("pressure_msl") val pressureMsl: Double? = null
)

@Serializable
data class OpenMeteoElevationResponse(
    val elevation: List<Double>? = null
)

@Serializable
data class AviationMetarResponse(
    val icaoId: String? = null,
    val rawOb: String? = null,
    val wdir: Int? = null,
    val wspd: Int? = null,
    val temp: Double? = null,
    val dewp: Double? = null,
    val altim: Double? = null,
    val visib: String? = null,
    val clouds: List<MetarCloud>? = null
)

@Serializable
data class MetarCloud(
    val cover: String? = null,
    val base: Int? = null
)
