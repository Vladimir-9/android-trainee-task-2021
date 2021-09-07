package com.project.weatherapp.data

import com.project.weatherapp.remote.RemoteDailyForecast
import com.project.weatherapp.remote.RemoteWeatherInTheCity
import retrofit2.Response

interface Repository {

    suspend fun getCoordinatesCity(cityName: String): Response<RemoteWeatherInTheCity>
    suspend fun getDailyForecast(latitude: Double, longitude: Double): Response<RemoteDailyForecast>
    suspend fun getWeatherInTheCityByCoordinates(
        latitude: Double,
        longitude: Double
    ): Response<RemoteWeatherInTheCity>
}