package com.amlcdesign.roadpulsecollector.manager

import android.content.Context
import android.os.Build
import android.provider.Settings

import java.io.File
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.enums.StopReason
import com.amlcdesign.roadpulsecollector.enums.VehicleType

import com.amlcdesign.roadpulsecollector.model.DeviceInfo
import com.amlcdesign.roadpulsecollector.model.EventRecord
import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.model.Ride
import com.amlcdesign.roadpulsecollector.model.RideInfo
import com.amlcdesign.roadpulsecollector.model.VehicleInfo

import com.amlcdesign.roadpulsecollector.storage.AccelerometerCsvWriter
import com.amlcdesign.roadpulsecollector.storage.EventsCsvWriter
import com.amlcdesign.roadpulsecollector.storage.GpsCsvWriter
import com.amlcdesign.roadpulsecollector.storage.GyroscopeCsvWriter
import com.amlcdesign.roadpulsecollector.storage.RideFolderManager

import com.amlcdesign.roadpulsecollector.sensor.AccelerometerManager
import com.amlcdesign.roadpulsecollector.sensor.GyroscopeManager
import com.amlcdesign.roadpulsecollector.storage.HistoryManager

import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger


class RideManager(
    private val context: Context
) {

    // =========================================================
    // ACTIVE RIDE
    // =========================================================

    /*
     * RideManager is the owner of the active ride lifecycle.
     *
     * At any point:
     *      0 active rides
     *              OR
     *      1 active RECORDING ride
     *
     * Ride folders are NOT scanned to determine this state.
     */
    private var currentRide: Ride? = null

    private var rideStopping = false

    private val historyManager =
        HistoryManager(context)


    // =========================================================
    // STORAGE / COLLECTION
    // =========================================================

    private var gpsCsvWriter: GpsCsvWriter? = null
    private var eventsCsvWriter: EventsCsvWriter? = null

    private var accelerometerManager: AccelerometerManager? = null
    private var accelerometerCsvWriter: AccelerometerCsvWriter? = null

    private var gyroscopeManager: GyroscopeManager? = null
    private var gyroscopeCsvWriter: GyroscopeCsvWriter? = null


    // =========================================================
    // START RIDE
    // =========================================================

    fun startRide(
        rideMode: RideMode = RideMode.MANUAL,
        vehicleType: VehicleType = VehicleType.CAR
    ): Ride {

        /*
         * One active ride maximum.
         *
         * If a RECORDING ride already exists, do not create
         * another ride and do not create another ride folder.
         *
         * The UI should never expose START while RECORDING,
         * but RideManager also protects the lifecycle internally.
         */
        currentRide?.let { ride ->

            if (ride.ride.status == RideStatus.RECORDING) {

                RoadPulseLogger.ride(
                    "RIDE START IGNORED | active=${ride.ride.rideId}"
                )

                return ride
            }
        }

        rideStopping = false


        // ---------------------------------------------------------
        // Create Ride ID
        // ---------------------------------------------------------

        val id = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())


        // ---------------------------------------------------------
        // Ride start information
        // ---------------------------------------------------------

        val startEpoch =
            System.currentTimeMillis()

        val startIso =
            OffsetDateTime.now()
                .format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )


        val startReason =
            when (rideMode) {
                RideMode.AUTO -> "AUTO_START"
                RideMode.MANUAL -> "MANUAL"
            }


        val rideInfo = RideInfo(

            rideId = "RP_$id",

            rideUuid = UUID.randomUUID().toString(),

            rideMode = rideMode,

            status = RideStatus.RECORDING,

            startReason = startReason,

            startEpoch = startEpoch,

            startIso = startIso
        )


        val ride = Ride(

            device = getDeviceInfo(),

            vehicle = VehicleInfo(
                vehicleType = vehicleType
            ),

            ride = rideInfo
        )


        // ---------------------------------------------------------
        // Create Ride Folder
        // ---------------------------------------------------------

        val rideFolder =
            RideFolderManager(context)
                .createRideFolder(ride)


        // ---------------------------------------------------------
        // GPS CSV
        // ---------------------------------------------------------

        val gpsFile =
            File(
                rideFolder,
                "gps.csv"
            )

        gpsCsvWriter =
            GpsCsvWriter(gpsFile)

        gpsCsvWriter?.initialize()


        // ---------------------------------------------------------
        // EVENTS CSV
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
        // ACCELEROMETER CSV
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


        // ---------------------------------------------------------
        // ACCELEROMETER MANAGER
        // ---------------------------------------------------------

        accelerometerManager =
            AccelerometerManager(context) { record ->

                accelerometerCsvWriter?.write(record)

                currentRide?.let { activeRide ->

                    if (
                        activeRide.ride.status ==
                        RideStatus.RECORDING
                    ) {

                        activeRide.data.accelerometerSamples =
                            activeRide.data.accelerometerSamples + 1
                    }
                }
            }


        // ---------------------------------------------------------
        // GYROSCOPE CSV
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


        // ---------------------------------------------------------
        // GYROSCOPE MANAGER
        // ---------------------------------------------------------

        gyroscopeManager =
            GyroscopeManager(context) { record ->

                gyroscopeCsvWriter?.write(record)

                currentRide?.let { activeRide ->

                    if (
                        activeRide.ride.status ==
                        RideStatus.RECORDING
                    ) {

                        activeRide.data.gyroscopeSamples =
                            activeRide.data.gyroscopeSamples + 1
                    }
                }
            }


        // ---------------------------------------------------------
        // ACTIVE RIDE
        // ---------------------------------------------------------

        currentRide = ride

        historyManager.createHistoryEntry(ride)

        // ---------------------------------------------------------
        // RIDE START EVENT
        // ---------------------------------------------------------

        recordEvent(
            eventType = "RIDE_STARTED",
            eventValue = rideMode.name
        )


        RoadPulseLogger.ride(
            "RIDE STARTED | id=${ride.ride.rideId} | mode=${rideMode.name}"
        )


        // ---------------------------------------------------------
        // Start sensors
        // ---------------------------------------------------------

        startAccelerometer()
        startGyroscope()


        return ride
    }


    // =========================================================
    // STOP RIDE
    // =========================================================

    fun stopRide(
        stopReason: StopReason
    ) {

        val ride =
            currentRide
                ?: return


        if (
            ride.ride.status !=
            RideStatus.RECORDING
        ) {
            return
        }


        if (rideStopping) {
            return
        }


        rideStopping = true


        val rideId =
            ride.ride.rideId


        RoadPulseLogger.ride(
            "RIDE STOP | id=$rideId | reason=${stopReason.name}"
        )


        // ---------------------------------------------------------
        // Stop sensors first
        // ---------------------------------------------------------

        stopAccelerometer()
        stopGyroscope()


        // ---------------------------------------------------------
        // Finalize ride metadata
        // ---------------------------------------------------------

        val endEpoch =
            System.currentTimeMillis()

        val endIso =
            OffsetDateTime.now()
                .format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )


        ride.ride.endEpoch =
            endEpoch

        ride.ride.endIso =
            endIso

        ride.ride.status =
            RideStatus.COMPLETED

        ride.ride.durationSeconds =
            (
                    (endEpoch - ride.ride.startEpoch) / 1000
                    ).toInt()

        ride.ride.stopReason =
            stopReason.name


        // ---------------------------------------------------------
        // Record STOP event
        // ---------------------------------------------------------

        recordEvent(
            eventType = "RIDE_STOPPED",
            eventValue = stopReason.name
        )


        // ---------------------------------------------------------
        // Save final ride.json
        // ---------------------------------------------------------

        RideFolderManager(context)
            .updateRide(ride)

        // ---------------------------------------------------------
        // Update History
        // ---------------------------------------------------------
        historyManager.updateHistoryEntry(ride)

        // ---------------------------------------------------------
        // Release storage references
        // ---------------------------------------------------------

        gpsCsvWriter = null
        eventsCsvWriter = null
        accelerometerCsvWriter = null
        gyroscopeCsvWriter = null


        // ---------------------------------------------------------
        // Active ride is now finished.
        //
        // History will be handled by the appropriate repository /
        // history layer later.
        //
        // RideManager must NOT keep a COMPLETED ride as the
        // current active ride.
        // ---------------------------------------------------------

        currentRide = null


        rideStopping = false


        RoadPulseLogger.ride(
            "RIDE COMPLETED | id=$rideId | duration=${ride.ride.durationSeconds}s"
        )
    }


    // =========================================================
    // GPS POINT COUNT
    // =========================================================

    fun incrementGpsPointCount() {

        currentRide?.let { ride ->

            if (
                ride.ride.status ==
                RideStatus.RECORDING
            ) {

                ride.data.gpsPoints =
                    ride.data.gpsPoints + 1
            }
        }
    }


    // =========================================================
    // RECORD GPS
    // =========================================================

    fun recordGps(
        record: GpsRecord
    ) {

        val ride =
            currentRide
                ?: return


        if (
            ride.ride.status !=
            RideStatus.RECORDING
        ) {
            return
        }


        gpsCsvWriter?.write(record)


        ride.data.gpsPoints =
            ride.data.gpsPoints + 1


        // Update accelerometer sampling profile
        accelerometerManager?.updateSpeed(
            record.speedKmh
        )
    }


    // =========================================================
    // ACCELEROMETER
    // =========================================================

    fun startAccelerometer() {

        accelerometerManager?.start()

        RoadPulseLogger.accel(
            "Accelerometer started"
        )
    }


    fun stopAccelerometer() {

        accelerometerManager?.stop()

        accelerometerManager = null

        RoadPulseLogger.accel(
            "Accelerometer stopped"
        )
    }


    // =========================================================
    // GYROSCOPE
    // =========================================================

    fun startGyroscope() {

        gyroscopeManager?.start()

        RoadPulseLogger.gyro(
            "Gyroscope started"
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

        return currentRide
    }


    // =========================================================
    // RIDE STATUS
    // =========================================================

    fun getRideStatus(): RideStatus {

        return currentRide?.ride?.status
            ?: RideStatus.IDLE
    }


    fun isRideActive(): Boolean {

        return getRideStatus() ==
                RideStatus.RECORDING
    }


    // =========================================================
    // DEVICE INFO
    // =========================================================

    private fun getDeviceInfo(): DeviceInfo {

        val deviceId =
            Settings.Secure.getString(
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


    // =========================================================
    // RECORD EVENT
    // =========================================================

    private fun recordEvent(
        eventType: String,
        eventValue: String = ""
    ) {

        val writer =
            eventsCsvWriter
                ?: return


        val now =
            System.currentTimeMillis()


        val timestampIso =
            OffsetDateTime.now()
                .format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )


        writer.write(

            EventRecord(

                timestampEpoch = now,

                timestampIso = timestampIso,

                eventType = eventType,

                eventValue = eventValue
            )
        )
    }
}