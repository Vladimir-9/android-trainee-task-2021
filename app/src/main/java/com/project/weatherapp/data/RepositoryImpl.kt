package com.project.weatherapp.data

import com.project.weatherapp.remote.network.ServerApi
import retrofit2.awaitResponse
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val serverApi: ServerApi) : Repository {

    override suspend fun getCoordinatesCity(cityName: String) =
        serverApi.getWeatherInTheCity(cityName).awaitResponse()

    override suspend fun getWeatherInTheCityByCoordinates(latitude: Double, longitude: Double) =
        serverApi.getWeatherInTheCityByCoordinates(latitude, longitude).awaitResponse()

    override suspend fun getDailyForecast(latitude: Double, longitude: Double) =
        serverApi.getDailForecast(latitude, longitude).awaitResponse()
}