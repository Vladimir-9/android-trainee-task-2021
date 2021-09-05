package com.project.weatherapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.weatherapp.R
import com.project.weatherapp.ui.today.TodayWeatherFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container_fragment, TodayWeatherFragment())
            .commit()
    }
}