package com.mskdevelopers.helipadbuddy.data.repository

import com.mskdevelopers.helipadbuddy.data.remote.ApiJson
import com.mskdevelopers.helipadbuddy.data.remote.AviationMetarResponse
import com.mskdevelopers.helipadbuddy.data.remote.HttpClientProvider

class MetarRepository(private val http: HttpClientProvider = HttpClientProvider()) {

    fun fetchMetar(icao: String): AviationMetarResponse? {
        val url = "https://aviationweather.gov/api/data/metar?ids=$icao&format=json"
        val body = http.get(url) ?: return null
        return try {
            val list = ApiJson.instance.decodeFromString<List<AviationMetarResponse>>(body)
            list.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    fun parseVisibilityKm(visib: String?): Float {
        if (visib.isNullOrBlank()) return 0f
        return try {
            when {
                visib.contains("SM") -> {
                    val miles = visib.replace("SM", "").trim().toFloatOrNull() ?: 0f
                    miles * 1.60934f
                }
                visib.endsWith("+") -> 10f
                else -> {
                    val value = visib.toFloatOrNull() ?: return 0f
                    if (value > 100f) value / 1000f else value * 1.60934f
                }
            }
        } catch (_: Exception) {
            0f
        }
    }

    fun parseMetarQnhHpa(altim: Double?): Float {
        val raw = altim?.toFloat() ?: return 0f
        return if (raw < 100f) raw * 33.8639f / 100f else raw
    }

    fun formatMetarSnapshot(metar: AviationMetarResponse?): String {
        if (metar == null) return "No METAR fetched."
        return buildString {
            appendLine("ICAO: ${metar.icaoId.orEmpty()}")
            appendLine("Wind: ${metar.wdir ?: "—"}° @ ${metar.wspd ?: "—"} kt")
            appendLine("Vis: ${"%.1f".format(parseVisibilityKm(metar.visib))} km (${metar.visib.orEmpty()})")
            appendLine("QNH: ${"%.0f".format(parseMetarQnhHpa(metar.altim))} hPa")
            appendLine("Wx: ${parseWeatherPhenomena(metar.rawOb).orEmpty().ifBlank { "—" }}")
            appendLine("Clouds: ${formatClouds(metar.clouds)}")
        }.trim()
    }

    private val wxTokenLabels = mapOf(
        "DZ" to "Drizzle",
        "RA" to "Rain",
        "SN" to "Snow",
        "SG" to "Snow Grains",
        "IC" to "Ice Crystals",
        "PL" to "Ice Pellets",
        "GR" to "Hail",
        "GS" to "Small Hail",
        "UP" to "Unknown Precip",
        "BR" to "Mist",
        "FG" to "Fog",
        "FU" to "Smoke",
        "VA" to "Volcanic Ash",
        "DU" to "Dust",
        "SA" to "Sand",
        "HZ" to "Haze",
        "PY" to "Spray",
        "PO" to "Dust Whirls",
        "SQ" to "Squall",
        "FC" to "Funnel Cloud",
        "SS" to "Sandstorm",
        "DS" to "Duststorm",
        "TS" to "Thunderstorm"
    )

    /**
     * Extracts present-weather phenomena from raw METAR (e.g. HZ, RA, -SN).
     * Returns null if no wx tokens found so callers can keep model/forecast text.
     */
    fun parseWeatherPhenomena(rawOb: String?): String? {
        if (rawOb.isNullOrBlank()) return null
        val body = rawOb.trim()
        val tokens = mutableListOf<String>()
        val regex = Regex("""([+-]?)(VC)?(MI|BC|PR|DR|BL|SH|TS|FZ)?(DZ|RA|SN|SG|IC|PL|GR|GS|UP|BR|FG|FU|VA|DU|SA|HZ|PY|PO|SQ|FC|SS|DS)""")
        regex.findAll(body).forEach { match ->
            val intensity = when (match.groupValues[1]) {
                "+" -> "Heavy "
                "-" -> "Light "
                else -> ""
            }
            val descriptor = when (match.groupValues[3]) {
                "SH" -> "Showers "
                "TS" -> "Thunderstorm "
                "FZ" -> "Freezing "
                "BL" -> "Blowing "
                "DR" -> "Low Drifting "
                else -> ""
            }
            val code = match.groupValues[4]
            val label = wxTokenLabels[code] ?: code
            tokens.add("$intensity$descriptor$label".trim())
        }
        return tokens.distinct().take(3).joinToString(", ").takeIf { it.isNotBlank() }
    }

    fun formatClouds(clouds: List<com.mskdevelopers.helipadbuddy.data.remote.MetarCloud>?): String {
        if (clouds.isNullOrEmpty()) return "CLR"
        return clouds.joinToString(" ") { c ->
            val cover = c.cover ?: "UNK"
            val base = c.base?.let { "$it" } ?: ""
            if (base.isNotEmpty()) "$cover $base" else cover
        }
    }

    data class ParsedCloudLayers(
        val low: String = "",
        val medium: String = "",
        val high: String = "",
        val ceilingFt: Int = 0
    )

    fun parseCloudLayers(clouds: List<com.mskdevelopers.helipadbuddy.data.remote.MetarCloud>?): ParsedCloudLayers {
        if (clouds.isNullOrEmpty()) return ParsedCloudLayers()
        var low = ""
        var medium = ""
        var high = ""
        var ceiling = Int.MAX_VALUE
        for (cloud in clouds) {
            val cover = cloud.cover.orEmpty()
            val base = cloud.base ?: continue
            val label = "$cover $base"
            when (cover.uppercase()) {
                "FEW", "SCT", "BKN", "OVC" -> {
                    if (base < 6500) {
                        low = label
                    } else if (base < 20000) {
                        medium = label
                    } else {
                        high = label
                    }
                    if (cover.equals("BKN", true) || cover.equals("OVC", true)) {
                        ceiling = minOf(ceiling, base)
                    }
                }
            }
        }
        return ParsedCloudLayers(
            low = low,
            medium = medium,
            high = high,
            ceilingFt = if (ceiling == Int.MAX_VALUE) 0 else ceiling
        )
    }
}
