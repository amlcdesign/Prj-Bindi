package com.amlcdesign.roadpulsecollector.manager

import android.content.Context
import android.util.Log
import com.amlcdesign.roadpulsecollector.model.Ride
import com.amlcdesign.roadpulsecollector.storage.RideFolderManager
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.*
import com.amlcdesign.roadpulsecollector.enums.RideStatus

class RideManager(
    private val context: Context
) {

    private var currentRide: Ride? = null

    fun startRide(): Ride {

        val id = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val ride = Ride(

            rideId = "RP_$id",

            rideUuid = UUID.randomUUID().toString(),

            status = RideStatus.RECORDING,

            startEpoch = System.currentTimeMillis(),

            startIso = OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )

        RideFolderManager(context)
            .createRideFolder(ride)

        currentRide = ride

        Log.d(
            "RoadPulse",
            "Ride Started : ${ride.rideId}"
        )

        return ride
    }

    fun stopRide() {

        currentRide?.endEpoch = System.currentTimeMillis()

        currentRide?.endIso =
            OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        currentRide?.status = RideStatus.COMPLETED

        currentRide?.durationSeconds =
            ((currentRide!!.endEpoch!! - currentRide!!.startEpoch) / 1000).toInt()

        Log.d(
            "RoadPulse",
            "Ride Stopped"
        )
    }

    fun getCurrentRide() = currentRide
}