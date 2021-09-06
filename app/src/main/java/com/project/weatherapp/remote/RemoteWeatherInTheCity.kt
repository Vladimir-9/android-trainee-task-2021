package com.project.weatherapp.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteWeatherInTheCity(
    @Json(name = "coord")
    val coordinates: Coordinates,
    val name: String
)

@JsonClass(generateAdapter = true)
data class Coordinates(
    @Json(name = "lon")
    val longitude: Double,
    @Json(name = "lat")
    val latitude: Double
)