package com.amlcdesign.roadpulsecollector.manager

import android.content.Context
import android.util.Log
import android.os.Build
import android.provider.Settings

import java.io.File
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.amlcdesign.roadpulsecollector.model.DeviceInfo
import com.amlcdesign.roadpulsecollector.model.DataInfo

import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.model.Ride
import com.amlcdesign.roadpulsecollector.model.RideInfo
import com.amlcdesign.roadpulsecollector.storage.RideFolderManager
import com.amlcdesign.roadpulsecollector.enums.RideMode

import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.storage.GpsCsvWriter

import com.amlcdesign.roadpulsecollector.enums.VehicleType
import com.amlcdesign.roadpulsecollector.model.VehicleInfo

import com.amlcdesign.roadpulsecollector.model.EventRecord
import com.amlcdesign.roadpulsecollector.storage.EventsCsvWriter

import com.amlcdesign.roadpulsecollector.sensor.AccelerometerManager
import com.amlcdesign.roadpulsecollector.storage.AccelerometerCsvWriter

class RideManager(
    private val context: Context
) {

    private var currentRide: Ride? = null

    private var gpsCsvWriter: GpsCsvWriter? = null
    private var eventsCsvWriter: EventsCsvWriter? = null

    private var accelerometerManager: AccelerometerManager? = null
    private var accelerometerCsvWriter: AccelerometerCsvWriter? = null

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
        // Create EVENTS CSV
        // ---------------------------------------------------------
        val eventsFile =
            File(
                rideFolder,
                "events.csv"
            )

        eventsCsvWriter =
            EventsCsvWriter(eventsFile)

        eventsCsvWriter?.initialize()

        // ---------------------------------------------------------
        // Create ACCELEROMETER Section
        // ---------------------------------------------------------

        val accelerometerFile =
            File(
                rideFolder,
                "accelerometer.csv"
            )

        accelerometerCsvWriter =
            AccelerometerCsvWriter(
                accelerometerFile
            )

        accelerometerCsvWriter?.initialize()

        //Manager
        accelerometerManager =
            AccelerometerManager(context) { record ->

                accelerometerCsvWriter?.write(record)

                currentRide?.data?.accelerometerSamples =
                    (currentRide?.data?.accelerometerSamples ?: 0) + 1
            }

        // ---------------------------------------------------------
        // Store current ride
        // ---------------------------------------------------------

        currentRide = ride

        recordEvent(
            eventType = "RIDE_STARTED",
            eventValue = rideMode.name
        )

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

            recordEvent(
                eventType = "RIDE_STOPPED",
                eventValue = ride.ride.stopReason
            )

        }



        gpsCsvWriter = null
        eventsCsvWriter = null
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

    fun startAccelerometer() {
        accelerometerManager?.start()
    }

    fun stopAccelerometer() {
        accelerometerManager?.stop()
        accelerometerManager = null
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

    private fun recordEvent(
        eventType: String,
        eventValue: String = ""
    ) {

        val now = System.currentTimeMillis()

        val timestampIso =
            OffsetDateTime.now()
                .format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )

        eventsCsvWriter?.write(
            EventRecord(
                timestampEpoch = now,
                timestampIso = timestampIso,
                eventType = eventType,
                eventValue = eventValue
            )
        )
    }

}