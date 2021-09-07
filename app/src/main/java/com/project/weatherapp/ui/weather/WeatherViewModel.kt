package com.project.weatherapp.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.weatherapp.data.Repository
import com.project.weatherapp.remote.RemoteDailyForecast
import com.project.weatherapp.remote.RemoteWeatherInTheCity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class TodayWeatherViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

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
        MutableLiveData<WeatherLoadState>()
    val getWeatherInTheCityByCoordinatesLiveData: LiveData<WeatherLoadState>
        get() = _getWeatherInTheCityByCoordinatesLiveData

    fun getCoordinatesCity(cityName: String) {
        viewModelScope.launch {
            runCatching {
                _getCoordinatesCityLiveData.postValue(WeatherLoadState.LoadState)
                val response = repository.getCoordinatesCity(cityName)
                if (response.isSuccessful) {
                    _getCoordinatesCityLiveData.postValue(
                        WeatherLoadState.Success(null, response)
                    )
                } else {
                    badRequest(response)
                }
            }.onFailure { throwable ->
                _getCoordinatesCityLiveData.postValue(WeatherLoadState.Error(throwable.javaClass.name))
            }
        }
    }

    fun getAllWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            runCatching {
                if (isNeedANewRequest(lat, lon)) {
                    latitude = lat
                    longitude = lon
                    response = repository.getDailyForecast(lat, lon)
                }
                if (response?.isSuccessful == true)
                    _getDailyWeatherForecastLiveData.postValue(
                        WeatherLoadState.Success(response, null
                        )
                    )
            }.onFailure { throwable ->
                _getDailyWeatherForecastLiveData.postValue(WeatherLoadState.Error(throwable.javaClass.name))
            }
        }
    }

    fun getWeatherInTheCityByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            runCatching {
                _getWeatherInTheCityByCoordinatesLiveData.postValue(WeatherLoadState.LoadState)
                val response = repository.getWeatherInTheCityByCoordinates(lat, lon)
                _getWeatherInTheCityByCoordinatesLiveData.postValue(
                    WeatherLoadState.Success(null, response)
                )
            }.onFailure { throwable ->
                _getWeatherInTheCityByCoordinatesLiveData.postValue(WeatherLoadState.Error(throwable.javaClass.name))
            }
        }
    }

    // If the user entered incorrect data in the search query,
    // the server responds with an error and sends errorBody
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