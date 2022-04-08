package com.jerryallanakshay.stocks

import java.util.*

class NewsData(title: String, date: Long, source: String, image: String, type: Int) {
    val title = title
    val date = getElapsedTime(date)
    val source = source
    val image = image
    val type = type

    fun getElapsedTime(time: Long): String {
        val calendar = Calendar.getInstance()
        val now = (calendar.timeInMillis)/1000
        var diff = now-time
        var measurement = "seconds"
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
}