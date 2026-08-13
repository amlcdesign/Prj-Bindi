package com.amlcdesign.roadpulsecollector.utils

import android.util.Log

object RoadPulseLogger {

    private const val TAG = "RoadPulse"

    fun ride(message: String) {
        Log.d("$TAG-RIDE", message)
    }

    fun gps(message: String) {
        Log.d("$TAG-GPS", message)
    }

    fun accel(message: String) {
        Log.d("$TAG-ACCEL", message)
    }

    fun gyro(message: String) {
        Log.d("$TAG-GYRO", message)
    }

    fun storage(message: String) {
        Log.d("$TAG-STORAGE", message)
    }

    fun csv(message: String) {
        Log.d("$TAG-CSV", message)
    }

    fun network(message: String) {
        Log.d("$TAG-NETWORK", message)
    }

    fun processing(message: String) {
        Log.d("$TAG-PROCESSING", message)
    }

    fun ui(message: String) {
        Log.d("$TAG-UI", message)
    }

    fun info(message: String) {
        Log.i(TAG, message)
    }

    fun warn(message: String) {
        Log.w(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}