package com.project.weatherapp.ui.today

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
    private var rationaleDialog: AlertDialog by autoCleared()
    private var gepPermission: ActivityResultLauncher<String> by autoCleared()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentTodayWeatherBinding.bind(view)
        locationManager =
            (requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        viewBinding.btWeatherHere.setOnClickListener {
            gepPermission.launch(REQUIRED_PERMISSION)
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
        gepPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                when {
                    granted -> {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0F,
                            locationListener
                        )
                        visibilityElementWhenLoading(true)
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

    private fun showLocationRationaleDialog(isRequestPermission: Boolean) {
        rationaleDialog = AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.message_rationale_dialog))
            .setPositiveButton("OK") { dialog, _ ->
                if (isRequestPermission)
                    gepPermission.launch(REQUIRED_PERMISSION)
                else
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .show()
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
                toast(getString(R.string.geodata_is_disabled))
                visibilityElementWhenLoading(false)
            }

            override fun onLocationChanged(location: Location) {
                viewModel.getWeatherInTheCityByCoordinates(location.latitude, location.longitude)
                locationManager.removeUpdates(locationListener)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }
    }

    private fun observeWeatherInTheCityByCoordinates() {
        viewModel.getWeatherInTheCityByCoordinatesLiveData.observe(viewLifecycleOwner) {
            val latitude = it?.body()?.coordinates?.latitude
            val longitude = it?.body()?.coordinates?.longitude
            val cityName = it?.body()?.name
            viewBinding.twCityName.text = cityName
            viewModel.getAllWeather(latitude!!, longitude!!)
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
                    visibilityElementWhenLoading(false)
                    Timber.e("City()Error = ${state.errorMessage}")
                    when (state.errorMessage) {
                        "city not found" -> toast(getString(R.string.city_not_found))
                        "Nothing to geocode" -> toast(getString(R.string.nothing_to_geocode))
                        "java.net.UnknownHostException" -> toast(getString(R.string.check_network))
                    }
                }
                is WeatherLoadState.LoadState -> {
                    visibilityElementWhenLoading(true)
                }
            }
        }
    }

    private fun observeDailyWeatherForecast() {
        viewModel.getDailyWeatherForecastLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherLoadState.Success -> {
                    visibilityElementWhenLoading(false)
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
                    visibilityElementWhenLoading(false)
                    toast(getString(R.string.failed_to_get_data))
                }
                is WeatherLoadState.LoadState -> {
                }
            }
        }
    }

    private fun visibilityElementWhenLoading(isVisible: Boolean) {
        viewBinding.progressBar.isVisible = isVisible
        viewBinding.recyclerView.isVisible = isVisible.not()
    }

    private fun hideKeyboard() {
        val imm =
            activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(viewBinding.weatherContent.windowToken, 0)
    }

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