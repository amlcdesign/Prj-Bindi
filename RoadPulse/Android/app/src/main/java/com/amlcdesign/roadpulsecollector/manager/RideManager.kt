package com.amlcdesign.roadpulsecollector.manager

import android.content.Context
import android.util.Log
import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.model.DataInfo
import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.model.Ride
import com.amlcdesign.roadpulsecollector.model.RideInfo
import com.amlcdesign.roadpulsecollector.storage.GpsCsvWriter
import com.amlcdesign.roadpulsecollector.storage.RideFolderManager
import java.io.File
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.os.Build
import android.provider.Settings
import com.amlcdesign.roadpulsecollector.model.DeviceInfo
import com.amlcdesign.roadpulsecollector.enums.VehicleType
import com.amlcdesign.roadpulsecollector.model.VehicleInfo

class RideManager(
    private val context: Context
) {

    private var currentRide: Ride? = null

    private var gpsCsvWriter: GpsCsvWriter? = null


    // =========================================================
    // START RIDE
    // =========================================================

    fun startRide(
        rideMode: RideMode = RideMode.MANUAL,
        vehicleType: VehicleType = VehicleType.CAR
    ): Ride {

        val id = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val rideInfo = RideInfo(

            rideId = "RP_$id",

            rideUuid = UUID.randomUUID().toString(),

            rideMode = rideMode,

            status = RideStatus.RECORDING,

            startReason = "MANUAL",

            startEpoch = System.currentTimeMillis(),

            startIso = OffsetDateTime.now()
                .format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )
        )

        val ride = Ride(
            device = getDeviceInfo(),
            vehicle = VehicleInfo(
                vehicleType = vehicleType
            ),
            ride = rideInfo
        )


        // ---------------------------------------------------------
        // Create ride folder
        // ---------------------------------------------------------

        RideFolderManager(context)
            .createRideFolder(ride)


        // ---------------------------------------------------------
        // Create GPS CSV
        // ---------------------------------------------------------

        val rideFolder =
            File(
                context.getExternalFilesDir(null),
                "RoadPulse/${ride.ride.rideId}"
            )

        val gpsFile =
            File(
                rideFolder,
                "gps.csv"
            )

        gpsCsvWriter =
            GpsCsvWriter(gpsFile)

        gpsCsvWriter?.initialize()


        // ---------------------------------------------------------
        // Store current ride
        // ---------------------------------------------------------

        currentRide = ride


        Log.d(
            "RoadPulse",
            "Ride Started : ${ride.ride.rideId}"
        )

        return ride
    }


    // =========================================================
    // STOP RIDE
    // =========================================================

    fun stopRide() {

        currentRide?.let { ride ->

            ride.ride.endEpoch =
                System.currentTimeMillis()

            ride.ride.endIso =
                OffsetDateTime.now()
                    .format(
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    )

            ride.ride.status =
                RideStatus.COMPLETED

            ride.ride.durationSeconds =
                (
                        (ride.ride.endEpoch!! -
                                ride.ride.startEpoch) / 1000
                        ).toInt()

            ride.ride.stopReason =
                "MANUAL"


            // -----------------------------------------------------
            // Save final ride.json
            // -----------------------------------------------------

            RideFolderManager(context)
                .updateRide(ride)


            Log.d(
                "RoadPulse",
                "Ride Completed : ${ride.ride.rideId}"
            )
        }


        gpsCsvWriter = null
    }


    // =========================================================
    // GPS POINT COUNT
    // =========================================================

    fun incrementGpsPointCount() {

        currentRide?.data?.let {

            it.gpsPoints =
                it.gpsPoints + 1
        }
    }


    // =========================================================
    // RECORD GPS
    // =========================================================

    fun recordGps(
        record: GpsRecord
    ) {

        gpsCsvWriter?.write(record)

        currentRide?.data?.let {

            it.gpsPoints =
                it.gpsPoints + 1
        }
    }


    // =========================================================
    // CURRENT RIDE
    // =========================================================

    fun getCurrentRide(): Ride? {

        return currentRide
    }


    // =========================================================
    // GET DEVICE INFO
    // =========================================================
    private fun getDeviceInfo(): DeviceInfo {

        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""

        return DeviceInfo(

            deviceId = deviceId,

            manufacturer = Build.MANUFACTURER,

            model = Build.MODEL,

            os = "Android",

            osVersion = Build.VERSION.RELEASE
        )
    }


}