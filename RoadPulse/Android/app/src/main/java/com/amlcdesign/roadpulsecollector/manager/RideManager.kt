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
import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.storage.GpsCsvWriter
import java.io.File


class RideManager(
    private val context: Context
) {

    private var currentRide: Ride? = null
    private var gpsCsvWriter: GpsCsvWriter? = null

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

        val rideFolder =
            File(
                context.getExternalFilesDir(null),
                "RoadPulse/${ride.rideId}"
            )

        val gpsFile =
            File(
                rideFolder,
                "gps.csv"
            )

        gpsCsvWriter =
            GpsCsvWriter(gpsFile)

        gpsCsvWriter?.initialize()

        currentRide = ride

        Log.d(
            "RoadPulse",
            "Ride Started : ${ride.rideId}"
        )

        return ride
    }

    fun stopRide() {

        currentRide?.let { ride ->

            ride.endEpoch =
                System.currentTimeMillis()

            ride.endIso =
                OffsetDateTime.now()
                    .format(
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    )

            ride.status =
                RideStatus.COMPLETED

            ride.durationSeconds =
                (
                        (ride.endEpoch!! - ride.startEpoch)
                                / 1000
                        ).toInt()

            RideFolderManager(context)
                .updateRide(ride)

            Log.d(
                "RoadPulse",
                "Ride Completed : ${ride.rideId}"
            )
        }

        gpsCsvWriter = null
    }

    fun incrementGpsPointCount() {

        currentRide?.gpsPoints =
            currentRide!!.gpsPoints + 1
    }

    fun recordGps(record: GpsRecord) {

        gpsCsvWriter?.write(record)

        currentRide?.gpsPoints =
            (currentRide?.gpsPoints ?: 0) + 1
    }

    fun getCurrentRide() = currentRide
}