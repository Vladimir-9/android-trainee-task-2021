package com.project.weatherapp.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteDailyForecast(
    val daily: List<Day>
)

@JsonClass(generateAdapter = true)
data class Day(
    val dt: Int,
    val temp: Temp,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<ListWeather>
)

@JsonClass(generateAdapter = true)
data class ListWeather(
    val id: Int,
    val description: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class Temp(
    val day: Double,
    val night: Double
)