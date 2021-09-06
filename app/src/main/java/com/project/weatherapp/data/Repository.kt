package com.project.weatherapp.data

import com.project.weatherapp.remote.RemoteWeatherInTheCity
import retrofit2.Response

interface Repository {

    suspend fun getTodayWeather(cityName: String): Response<RemoteWeatherInTheCity>
}