package com.project.weatherapp.data

import com.project.weatherapp.remote.network.Networking
import retrofit2.awaitResponse

class RepositoryImpl : Repository {

    override suspend fun getTodayWeather(cityName: String) =
        Networking.networkingApi.getWeatherInTheCity(cityName).awaitResponse()

    suspend fun getWeatherInTheCityByCoordinates(latitude: Double, longitude: Double) =
        Networking.networkingApi.getWeatherInTheCityByCoordinates(latitude, longitude).awaitResponse()

    suspend fun getDailyForecast(latitude: Double, longitude: Double) =
        Networking.networkingApi.getDailForecast(latitude, longitude).awaitResponse()
}