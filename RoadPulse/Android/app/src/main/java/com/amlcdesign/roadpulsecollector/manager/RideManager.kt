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
import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.StopReason
import com.amlcdesign.roadpulsecollector.enums.VehicleType

import com.amlcdesign.roadpulsecollector.model.Ride
import com.amlcdesign.roadpulsecollector.model.RideInfo
import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.model.VehicleInfo
import com.amlcdesign.roadpulsecollector.model.EventRecord

import com.amlcdesign.roadpulsecollector.storage.RideFolderManager
import com.amlcdesign.roadpulsecollector.storage.GpsCsvWriter
import com.amlcdesign.roadpulsecollector.storage.EventsCsvWriter
import com.amlcdesign.roadpulsecollector.storage.AccelerometerCsvWriter
import com.amlcdesign.roadpulsecollector.storage.GyroscopeCsvWriter

import com.amlcdesign.roadpulsecollector.sensor.AccelerometerManager
import com.amlcdesign.roadpulsecollector.sensor.GyroscopeManager

import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger
class RideManager(
    private val context: Context
) {

    private var gpsCsvWriter: GpsCsvWriter? = null
    private var eventsCsvWriter: EventsCsvWriter? = null

    private var accelerometerManager: AccelerometerManager? = null
    private var accelerometerCsvWriter: AccelerometerCsvWriter? = null

    private var gyroscopeManager: GyroscopeManager? = null
    private var gyroscopeCsvWriter: GyroscopeCsvWriter? = null

    private var rideStopping = false
    private var currentRide: Ride? = null

    init {
        val unfinishedRide = getUnfinishedRide()

        RoadPulseLogger.ride(
            "RideManager INIT | unfinishedRide=${
                unfinishedRide?.ride?.rideId ?: "NONE"
            }"
        )
    }

    // =========================================================
    // START RIDE
    // =========================================================

    fun startRide(
        rideMode: RideMode = RideMode.MANUAL,
        vehicleType: VehicleType = VehicleType.CAR
    ): Ride {

        if (isRideActive()) {
            RoadPulseLogger.ride(
                "START IGNORED | Ride already active | Ride ID = ${currentRide?.ride?.rideId}"
            )
            return currentRide!!
        }

        rideStopping = false

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
        // GYROSCOPE Section
        // ---------------------------------------------------------

        val gyroscopeFile =
            File(
                rideFolder,
                "gyroscope.csv"
            )

        gyroscopeCsvWriter =
            GyroscopeCsvWriter(
                gyroscopeFile
            )

        gyroscopeCsvWriter?.initialize()

        // Manager
        gyroscopeManager =
            GyroscopeManager(context) { record ->

                gyroscopeCsvWriter?.write(record)

                currentRide?.data?.gyroscopeSamples =
                    (currentRide?.data?.gyroscopeSamples ?: 0) + 1
            }

        // ---------------------------------------------------------
        // Store current ride
        // ---------------------------------------------------------

        currentRide = ride

        recordEvent(
            eventType = "RIDE_STARTED",
            eventValue = rideMode.name
        )

        RoadPulseLogger.ride(
            "Ride Started : ${ride.ride.rideId}"
        )

        // ---------------------------------------------------------
        // Start accelerometer
        // Start gyroscope
        // Storage + currentRide are now ready
        // ---------------------------------------------------------

        startAccelerometer()
        startGyroscope()

        return ride
    }


    // =========================================================
    // STOP RIDE
    // =========================================================

    fun stopRide(stopReason: StopReason) {

        RoadPulseLogger.ride(
            "STOP RIDE CALLED | reason=$stopReason"
        )

        if (currentRide == null) {
            RoadPulseLogger.ride(
                "STOP IGNORED | No current ride | Reason = ${stopReason.name}"
            )
            return
        }
        if (rideStopping) {
            RoadPulseLogger.ride(
                "STOP IGNORED | No current ride | Reason = ${stopReason.name}"
            )
            return
        }

        rideStopping = true
        stopAccelerometer()
        stopGyroscope()

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
                stopReason.name


            // -----------------------------------------------------
            // Save final ride.json
            // -----------------------------------------------------

            RoadPulseLogger.ride(
                "Recording RIDE_STOPPED event | Reason = ${stopReason.name}"
            )

            recordEvent(
                eventType = "RIDE_STOPPED",
                eventValue = ride.ride.stopReason
            )

            RoadPulseLogger.ride(
                "RIDE_STOPPED event recorded | Reason = ${stopReason.name}"
            )
            RideFolderManager(context)
                .updateRide(ride)

            RoadPulseLogger.ride(
                "Ride Completed : ${ride.ride.rideId} | Stop Reason = ${stopReason.name}"
            )

        }

        gpsCsvWriter = null
        eventsCsvWriter = null
        accelerometerCsvWriter = null
        gyroscopeCsvWriter = null

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

        // Update accelerometer sampling profile
        accelerometerManager?.updateSpeed(
            record.speedKmh
        )
    }

    fun startAccelerometer() {
        accelerometerManager?.start()
        RoadPulseLogger.accel(
            "Accelerometer starting"
        )
    }

    fun stopAccelerometer() {
        accelerometerManager?.stop()
        accelerometerManager = null

        RoadPulseLogger.accel(
            "Accelerometer stopped"
        )
    }

    fun startGyroscope() {
        gyroscopeManager?.start()

        RoadPulseLogger.gyro(
            "Gyroscope starting"
        )
    }

    fun stopGyroscope() {
        gyroscopeManager?.stop()
        gyroscopeManager = null

        RoadPulseLogger.gyro(
            "Gyroscope stopped"
        )
    }

    // =========================================================
    // CURRENT RIDE
    // =========================================================

    fun getCurrentRide(): Ride? {

        RoadPulseLogger.ride(
            "getCurrentRide | ${
                currentRide?.ride?.rideId ?: "NONE"
            }"
        )

        return currentRide
    }

    // =========================================================
    // Check RIDE Status
    // =========================================================
    fun getRideStatus(): RideStatus {
        return currentRide?.ride?.status
            ?: RideStatus.IDLE
    }

    fun isRideActive(): Boolean {
        return getRideStatus() == RideStatus.RECORDING
    }

    fun getUnfinishedRide(): Ride? {

        return RideFolderManager(context)
            .getUnfinishedRide()
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