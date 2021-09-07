package com.project.weatherapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.weatherapp.R
import com.project.weatherapp.ui.weather.WeatherFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container_fragment, WeatherFragment())
            .commit()
    }
}