package com.project.weatherapp.ui.today

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.project.weatherapp.R
import com.project.weatherapp.databinding.FragmentTodayWeatherBinding
import com.project.weatherapp.ui.adapter.AdapterForWeather
import com.project.weatherapp.utility.WeatherItemDecoration
import com.project.weatherapp.utility.autoCleared
import com.project.weatherapp.utility.toast
import timber.log.Timber

class TodayWeatherFragment : Fragment(R.layout.fragment_today_weather) {

    private var viewBinding: FragmentTodayWeatherBinding by autoCleared()
    private val viewModel: TodayWeatherViewModel by viewModels()
    private var adapterWeather: AdapterForWeather by autoCleared()
    private var locationManager: LocationManager by autoCleared()
    private var locationListener: LocationListener by autoCleared()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentTodayWeatherBinding.bind(view)
        locationManager =
            (requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        viewBinding.btSearch.setOnClickListener {
            val searchRequest = viewBinding.edSearch.text.toString()
            if (searchRequest.isNotEmpty())
                viewModel.getCoordinatesCity(searchRequest)
            else
                toast(getString(R.string.empty_request))
        }

        initLocationListener()
        observeDailyWeatherForecast()
        initRecyclerView()
        observeCoordinatesCity()
        observeWeatherInTheCityByCoordinates()
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onClickLocationSettings()
            Timber.e("return")
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0F,
            locationListener
        )
    }

    private fun onClickLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun initRecyclerView() {
        adapterWeather = AdapterForWeather()
        with(viewBinding.recyclerView) {
            adapter = adapterWeather
            addItemDecoration(WeatherItemDecoration())
            setHasFixedSize(true)
        }
    }

    private fun initLocationListener() {
        locationListener = object : LocationListener {
            override fun onProviderDisabled(provider: String) {
                Timber.e("onProviderDisabled $provider")
            }

            override fun onProviderEnabled(provider: String) {
                Timber.e("onProviderEnabled $provider")
            }

            override fun onLocationChanged(location: Location) {
                viewModel.get(location.latitude, location.longitude)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    Timber.e("onStatusChanged $provider")
                }
            }
        }
    }

    private fun observeWeatherInTheCityByCoordinates() {
        viewModel.getWeatherInTheCityByCoordinatesLiveData.observe(viewLifecycleOwner) {
            val latitude = it?.body()?.coordinates?.latitude
            val longitude = it?.body()?.coordinates?.longitude
            val cityName = it?.body()?.name
            viewBinding.twCityName.text = cityName
            viewModel.getAllWeather(latitude!!, longitude!!)
            locationManager.removeUpdates(locationListener)
        }
    }

    private fun observeCoordinatesCity() {
        viewModel.getCoordinatesCityLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherLoadState.Success -> {
                    Timber.e("Success")
                    val latitude = state.coordinatesCity?.body()?.coordinates?.latitude
                    val longitude = state.coordinatesCity?.body()?.coordinates?.longitude
                    val cityName = state.coordinatesCity?.body()?.name
                    viewBinding.twCityName.text = cityName
                    viewModel.getAllWeather(latitude!!, longitude!!)
                }
                is WeatherLoadState.Error -> {
                    when (state.errorMessage) {
                        "city not found" -> toast(getString(R.string.city_not_found))
                        "Nothing to geocode" -> toast(getString(R.string.nothing_to_geocode))
                    }
                }
                is WeatherLoadState.LoadState -> {
                }
            }
        }
    }

    private fun observeDailyWeatherForecast() {
        viewModel.getDailyWeatherForecastLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherLoadState.Success -> {
                    val dataOfTheWeather = state.remoteDailyForecast?.body()?.daily
                    adapterWeather.items = dataOfTheWeather?.subList(0, 1)

                    viewBinding.btToday.setOnClickListener {
                        adapterWeather.items = dataOfTheWeather?.subList(0, 1)
                    }

                    viewBinding.btWeek.setOnClickListener {
                        adapterWeather.items = dataOfTheWeather
                    }
                }
                is WeatherLoadState.Error -> {
                    Timber.e("Error = ${state.errorMessage}")
                }
                is WeatherLoadState.LoadState -> {
                }
            }
        }
    }
}