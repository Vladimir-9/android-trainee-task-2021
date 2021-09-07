package com.project.weatherapp.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.project.weatherapp.R
import com.project.weatherapp.databinding.FragmentWeatherBinding
import com.project.weatherapp.ui.adapter.AdapterForWeather
import com.project.weatherapp.utility.WeatherItemDecoration
import com.project.weatherapp.utility.autoCleared
import com.project.weatherapp.utility.toast
import dagger.hilt.android.AndroidEntryPoint

/**
 * Due to the fact that the free version of the site API is used,
 * we have to make a double request to the server. Because,
 * the weather for several days can only be obtained by coordinates.
 * Therefore, to get the coordinates of a city, a request is made by the name of the city
 * from which the coordinates come and a second request for the weather for a week is made.
 *
 * When requesting weather by geolocation, we also have to make a double request,
 * because when requesting by coordinates, the name of the city does not come,
 * so a second request is made to find out the name of the city.
 */

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var viewBinding: FragmentWeatherBinding by autoCleared()
    private val viewModel: TodayWeatherViewModel by viewModels()
    private var adapterWeather: AdapterForWeather by autoCleared()
    private var locationManager: LocationManager by autoCleared()
    private var locationListener: LocationListener by autoCleared()
    private var rationaleDialog: AlertDialog by autoCleared()
    private var getPermission: ActivityResultLauncher<String> by autoCleared()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentWeatherBinding.bind(view)
        locationManager =
            (requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        viewBinding.btWeatherHere.setOnClickListener {
            getPermission.launch(REQUIRED_PERMISSION)
        }
        viewBinding.btSearch.setOnClickListener {
            reactionToClickingSearch()
        }

        initLocationListener()
        observeDailyWeatherForecast()
        initRecyclerView()
        observeCoordinatesCity()
        observeWeatherInTheCityByCoordinates()
        registerResultContracts()
    }

    @SuppressLint("MissingPermission")
    private fun registerResultContracts() {
        getPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                when {
                    granted -> {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0F,
                            locationListener
                        )
                        actionsAtTheLoading(true)
                    }
                    shouldShowRequestPermissionRationale(REQUIRED_PERMISSION) -> {
                        showLocationRationaleDialog(true)
                    }
                    else -> {
                        showLocationRationaleDialog(false)
                    }
                }
            }
    }

    // The dialog is shown to the user to explain why the geolocation permission is requested
    private fun showLocationRationaleDialog(isRequestPermission: Boolean) {
        rationaleDialog = AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.message_rationale_dialog))
            .setPositiveButton("OK") { dialog, _ ->
                if (isRequestPermission)
                // if the user clicked deny
                    getPermission.launch(REQUIRED_PERMISSION)
                else
                // if the user clicked deny and don't ask again
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun initLocationListener() {
        locationListener = object : LocationListener {
            override fun onProviderDisabled(provider: String) {
                toast(getString(R.string.geodata_is_disabled))
                actionsAtTheLoading(false)
            }

            override fun onLocationChanged(location: Location) {
                viewModel.getWeatherInTheCityByCoordinates(location.latitude, location.longitude)
                locationManager.removeUpdates(locationListener)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }
    }

    private fun initRecyclerView() {
        adapterWeather = AdapterForWeather()
        with(viewBinding.recyclerView) {
            adapter = adapterWeather
            addItemDecoration(WeatherItemDecoration())
            setHasFixedSize(true)
        }
    }

    // Here we get the coordinates of the city from geolocation (gps).
    // And also the name of the city, and we make a second request for a weekly forecast.
    private fun observeWeatherInTheCityByCoordinates() {
        viewModel.getWeatherInTheCityByCoordinatesLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherLoadState.Success -> {
                    val latitude = state.coordinatesCity?.body()?.coordinates?.latitude
                    val longitude = state.coordinatesCity?.body()?.coordinates?.longitude
                    val cityName = state.coordinatesCity?.body()?.name
                    viewBinding.twCityName.text = cityName
                    viewModel.getAllWeather(latitude!!, longitude!!)
                }
                is WeatherLoadState.Error -> {
                    actionsAtTheLoading(false)
                    toast(getString(R.string.check_network))
                }
                is WeatherLoadState.LoadState -> {
                }
            }
        }
    }

    // Here we get the coordinates of the city to make a repeated request
    // for the daily weather forecast
    private fun observeCoordinatesCity() {
        viewModel.getCoordinatesCityLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherLoadState.Success -> {
                    val latitude = state.coordinatesCity?.body()?.coordinates?.latitude
                    val longitude = state.coordinatesCity?.body()?.coordinates?.longitude
                    val cityName = state.coordinatesCity?.body()?.name
                    viewBinding.twCityName.text = cityName
                    viewModel.getAllWeather(latitude!!, longitude!!)
                }
                is WeatherLoadState.Error -> {
                    actionsAtTheLoading(false)
                    when (state.errorMessage) {
                        "city not found" -> toast(getString(R.string.city_not_found))
                        "Nothing to geocode" -> toast(getString(R.string.nothing_to_geocode))
                        "java.net.UnknownHostException" -> toast(getString(R.string.check_network))
                    }
                }
                is WeatherLoadState.LoadState -> {
                    actionsAtTheLoading(true)
                }
            }
        }
    }

    // Here we get the Daily weather forecast by coordinates
    private fun observeDailyWeatherForecast() {
        viewModel.getDailyWeatherForecastLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherLoadState.Success -> {
                    actionsAtTheLoading(false)
                    val dataOfTheWeather = state.remoteDailyForecast?.body()?.daily
                    adapterWeather.items = dataOfTheWeather?.subList(0, 1)
                    // Added ClickListener to switch between the weather today and for the week
                    viewBinding.btToday.setOnClickListener {
                        adapterWeather.items = dataOfTheWeather?.subList(0, 1)
                    }
                    // Added ClickListener to switch between the weather today and for the week
                    viewBinding.btWeek.setOnClickListener {
                        adapterWeather.items = dataOfTheWeather
                    }
                }
                is WeatherLoadState.Error -> {
                    actionsAtTheLoading(false)
                    toast(getString(R.string.failed_to_get_data))
                }
                is WeatherLoadState.LoadState -> {
                }
            }
        }
    }

    private fun actionsAtTheLoading(isLoading: Boolean) {
        viewBinding.progressBar.isVisible = isLoading
        viewBinding.recyclerView.isVisible = isLoading.not()
        viewBinding.btWeatherHere.isEnabled = isLoading.not()
        viewBinding.btSearch.isEnabled = isLoading.not()
    }

    private fun hideKeyboard() {
        val imm =
            activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(viewBinding.weatherContent.windowToken, 0)
    }

    // Checks whether the search query is empty
    private fun reactionToClickingSearch() {
        val searchRequest = viewBinding.edSearch.text.toString().trim()
        if (searchRequest.isNotEmpty()) {
            viewModel.getCoordinatesCity(searchRequest)
            hideKeyboard()
        } else {
            toast(getString(R.string.empty_request))
        }
    }

    companion object {
        const val REQUIRED_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }
}