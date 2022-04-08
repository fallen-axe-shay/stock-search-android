package com.jerryallanakshay.stocks

import java.text.SimpleDateFormat
import java.util.*

class NewsData(title: String, date: Long, source: String, image: String, url: String, summary: String, type: Int = 1) {
    val formattedDate = getFormattedDate(date)
    val title = title
    val difference = getTimeDifference(date)
    val date = getElapsedTime()
    val source = source
    val image = image
    var type = type
    val url = url
    val summary = summary

    fun getFormattedDate(date: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy")
        val netDate = Date(date * 1000)
        return sdf.format(netDate)
    }

    fun getElapsedTime(): String {
        var measurement = "seconds"
        var diff = difference
        if(diff>=(60 * 60)) {
            diff /= (60 * 60)
            measurement = "hours"
        } else if(diff>=(60)) {
            diff /=60
            measurement = "minutes"
        }
        if(diff.toInt()==1) {
            measurement = measurement.substring(0, -1)
        }
        return "${diff.toInt()} $measurement ago"
    }

    fun getTimeDifference(time: Long): Long {
        val calendar = Calendar.getInstance()
        val now = (calendar.timeInMillis)/1000
        var diff = now-time
        return diff
    }
}