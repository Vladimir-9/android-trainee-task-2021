package com.project.weatherapp.ui.today

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.weatherapp.data.RepositoryImpl
import com.project.weatherapp.remote.RemoteDailyForecast
import com.project.weatherapp.remote.RemoteWeatherInTheCity
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import kotlin.math.abs

class TodayWeatherViewModel : ViewModel() {

    private val repository = RepositoryImpl()
    private var response: Response<RemoteDailyForecast>? = null
    private var latitude = 0.0
    private var longitude = 0.0

    private val _getCoordinatesCityLiveData = MutableLiveData<WeatherLoadState>()
    val getCoordinatesCityLiveData: LiveData<WeatherLoadState>
        get() = _getCoordinatesCityLiveData

    private val _getDailyWeatherForecastLiveData = MutableLiveData<WeatherLoadState>()
    val getDailyWeatherForecastLiveData: LiveData<WeatherLoadState>
        get() = _getDailyWeatherForecastLiveData

    private val _getWeatherInTheCityByCoordinatesLiveData =
        MutableLiveData<Response<RemoteWeatherInTheCity>?>()
    val getWeatherInTheCityByCoordinatesLiveData: LiveData<Response<RemoteWeatherInTheCity>?>
        get() = _getWeatherInTheCityByCoordinatesLiveData

    fun getCoordinatesCity(cityName: String) {
        viewModelScope.launch {
            runCatching {
                _getCoordinatesCityLiveData.postValue(WeatherLoadState.LoadState)
                val response = repository.getTodayWeather(cityName)
                if (response.isSuccessful) {
                    _getCoordinatesCityLiveData.postValue(WeatherLoadState.Success(null, response))
                } else {
                    badRequest(response)
                }
            }.onFailure { throwable ->
                _getCoordinatesCityLiveData.postValue(WeatherLoadState.Error(throwable.javaClass.name))
            }
        }
    }

    fun getAllWeather(lat: Double = latitude, lon: Double = longitude) {
        viewModelScope.launch {
            runCatching {
                if (isNeedANewRequest(lat, lon)) {
                    latitude = lat
                    longitude = lon
                    response = repository.getDailyForecast(lat, lon)
                }
                if (response?.isSuccessful == true)
                    _getDailyWeatherForecastLiveData.value =
                        WeatherLoadState.Success(response, null)
            }.onFailure { throwable ->
                _getDailyWeatherForecastLiveData.postValue(WeatherLoadState.Error(throwable.javaClass.name))
            }
        }
    }

    fun getWeatherInTheCityByCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val response = repository.getWeatherInTheCityByCoordinates(latitude, longitude)
            _getWeatherInTheCityByCoordinatesLiveData.value = response
        }
    }

    private fun badRequest(response: Response<RemoteWeatherInTheCity>) {
        val errorBody = response.errorBody()?.string() ?: ""
        if (errorBody.isNotEmpty()) {
            val errorBodyMessage = JSONObject(errorBody).get("message")
            _getCoordinatesCityLiveData.value =
                WeatherLoadState.Error(errorBodyMessage.toString())
        }
    }

    private fun isNeedANewRequest(lat: Double, lon: Double) =
        response?.body() == null || isCompareTo(latitude, lat).not()
                || isCompareTo(longitude, lon).not()

    private fun isCompareTo(new: Double, old: Double) = abs(new - old) < EPSILON

    companion object {
        const val EPSILON: Double = 0.000001
    }
}

sealed class WeatherLoadState {
    data class Success(
        val remoteDailyForecast: Response<RemoteDailyForecast>?,
        val coordinatesCity: Response<RemoteWeatherInTheCity>?
    ) : WeatherLoadState()

    data class Error(val errorMessage: String?) : WeatherLoadState()
    object LoadState : WeatherLoadState()
}