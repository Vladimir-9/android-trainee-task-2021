package com.project.weatherapp.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.project.weatherapp.remote.Day

class AdapterForWeather : AsyncListDifferDelegationAdapter<Day>(WeatherDiffUtil()) {

    init {
        delegatesManager.addDelegate(WeatherAdapterDelegate())
    }

    class WeatherDiffUtil : DiffUtil.ItemCallback<Day>() {
        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.dt == newItem.dt
        }
    }
}