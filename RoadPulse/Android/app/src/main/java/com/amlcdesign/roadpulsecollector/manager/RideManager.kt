package com.amlcdesign.roadpulsecollector.manager

import com.amlcdesign.roadpulsecollector.model.Ride
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideManager {

    private var currentRide: Ride? = null

    fun startRide(): Ride {

        val id = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        currentRide = Ride(
            rideId = id,
            startTime = System.currentTimeMillis(),
            isRecording = true
        )

        return currentRide!!
    }

    fun stopRide() {

        currentRide?.endTime = System.currentTimeMillis()
        currentRide?.isRecording = false
    }

    fun getCurrentRide(): Ride? = currentRide
}