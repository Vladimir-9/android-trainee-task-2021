package com.project.weatherapp.utility

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

object Converters {

    fun convertDateFromUnix(date: Long): String {
        val instant = Instant.ofEpochSecond(date)
        val form = DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneId.systemDefault())
        return form.format(instant)
    }
}