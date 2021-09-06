package com.project.weatherapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import com.project.weatherapp.R
import com.project.weatherapp.databinding.ItemWeatherBinding
import com.project.weatherapp.remote.Day
import com.project.weatherapp.utility.Converters

class WeatherAdapterDelegate : AbsListItemAdapterDelegate<Day, Day, ViewHolder>() {

    override fun isForViewType(item: Day, items: MutableList<Day>, position: Int) = true

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_weather, parent, false))
    }

    override fun onBindViewHolder(item: Day, holder: ViewHolder, payloads: MutableList<Any>) {
        holder.bind(item)
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var viewBinding: ItemWeatherBinding

    fun bind(dayWeather: Day) {
        viewBinding = ItemWeatherBinding.bind(itemView)
        viewBinding.imageView.clipToOutline = true

        viewBinding.twDate.text =
            getText(R.string.date, Converters.convertDateFromUnix(dayWeather.dt.toLong()))
        viewBinding.twTemperature.text =
            getText(R.string.temperature, dayWeather.temp.day.toString())
        viewBinding.twHumidity.text =
            getText(R.string.humidity, dayWeather.humidity.toString())
        viewBinding.twSpeedWind.text =
            getText(R.string.speed_wind, dayWeather.wind_speed.toString())
        viewBinding.twDescription.text = dayWeather.weather[0].description
        loadImage(dayWeather.weather[0].icon)
    }

    private fun loadImage(iconType: String) {
        Glide.with(itemView)
            .load("https://openweathermap.org/img/wn/$iconType@2x.png")
            .into(viewBinding.imageView)
    }

    private fun getText(id: Int, text: String) = "${itemView.context.getString(id)} $text"
}