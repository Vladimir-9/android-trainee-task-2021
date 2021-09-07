package com.project.weatherapp.remote.network

import com.project.weatherapp.BuildConfig
import com.project.weatherapp.remote.RemoteDailyForecast
import com.project.weatherapp.remote.RemoteWeatherInTheCity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ServerApi {

    @GET("data/2.5/weather")
    fun getWeatherInTheCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("lang") lang: String = "ru"
    ): Call<RemoteWeatherInTheCity>

    @GET("data/2.5/weather")
    fun getWeatherInTheCityByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("lang") lang: String = "ru"
    ): Call<RemoteWeatherInTheCity>

    @GET("data/2.5/onecall")
    fun getDailForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = """minutely,hourly,alerts""",
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("lang") lang: String = "ru",
        @Query("units") units: String = "metric"
    ): Call<RemoteDailyForecast>
}