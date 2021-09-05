package com.project.weatherapp.data

import com.project.weatherapp.remote.RemoteWeather
import retrofit2.Call
import retrofit2.Response

interface Repository {

    suspend fun getTodayWeather(cityName: String): Response<RemoteWeather>
}