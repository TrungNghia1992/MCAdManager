package com.hn_ads

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

const val MILLISECONDS = 1000L

fun Context?.isNetworkAvailable(): Boolean {
    val cm = this?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    cm ?: return false
    val network = cm.activeNetwork
    if (network != null) {
        val networkCapabilities = cm.getNetworkCapabilities(network)
        return networkCapabilities != null
    }
    return false
}


fun Context.toastMessage(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_SHORT
    ).show()
}

fun Long.getTimeFlash(): Triple<Int, Int, Int> {
    val times = this.convertToSeconds().toFloat()
    val mathematical = times / (60 * 60)
    val hour = mathematical.toInt()
    val minus = (60 * (mathematical - hour)).toInt()
    val seconds = (times - (hour * 60 * 60) - (minus * 60)).toInt()

    return Triple(hour, minus, seconds)
}

fun Int.getTimeString(): String {
    return if (this.toString().length == 1) {
        "0$this"
    } else this.toString()
}


fun Int.convertToMilliseconds(): Int {
    return this * MILLISECONDS.toInt()
}

fun Long.convertToMilliseconds(): Long {
    return this * MILLISECONDS
}

private fun Long.convertToSeconds(): Long {
    return this / MILLISECONDS
}

fun Long.getTimeDiscount(): String {
    val secondsDiscount = this.convertToSeconds().toFloat()
    val mathematical = secondsDiscount / (60 * 60)
    val hour = mathematical.toInt()
    val minus = (60 * (mathematical - hour)).toInt()
    val seconds = (secondsDiscount - (hour * 60 * 60) - (minus * 60)).toInt()

    return "${hour.getTimeString()}:${minus.getTimeString()}:${seconds.getTimeString()}"
}