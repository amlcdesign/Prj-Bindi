package com.amlcdesign.roadpulsecollector.manager

import android.content.Context

import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.enums.StopReason
import com.amlcdesign.roadpulsecollector.enums.VehicleType

import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.model.Ride

import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger


class RideController(
    context: Context
) {

    private val rideManager =
        RideManager(context)


    // =========================================================
    // CURRENT APPLICATION MODE
    // =========================================================

    private var currentMode =
        RideMode.MANUAL


    // =========================================================
    // CONTROLLER CREATION
    // =========================================================

    init {
        RoadPulseLogger.ui(
            "RideController CREATED | hash=${hashCode()}"
        )
    }


    // =========================================================
    // MODE
    // =========================================================

    fun setMode(
        mode: RideMode
    ) {

        /*
         * Ride mode cannot be changed while a ride is recording.
         *
         * AUTO + RECORDING
         *      ↓
         * MANUAL transition is disabled
         *
         * Likewise, MANUAL + RECORDING cannot be changed to AUTO.
         */
        if (getRideStatus() == RideStatus.RECORDING) {

            RoadPulseLogger.ui(
                "MODE CHANGE IGNORED | recording=${getCurrentRide()?.ride?.rideId}"
            )

            return
        }


        currentMode = mode


        RoadPulseLogger.ui(
            "MODE CHANGED | mode=${mode.name}"
        )
    }


    fun getMode(): RideMode {
        return currentMode
    }


    // =========================================================
    // RIDE STATUS
    // =========================================================

    fun getRideStatus(): RideStatus {

        return rideManager.getRideStatus()
    }


    fun isRideActive(): Boolean {

        return getRideStatus() ==
                RideStatus.RECORDING
    }


    // =========================================================
    // CURRENT RIDE
    // =========================================================

    fun getCurrentRide(): Ride? {

        return rideManager.getCurrentRide()
    }


    // =========================================================
    // START RIDE
    // =========================================================

    fun startRide(
        vehicleType: VehicleType = VehicleType.CAR
    ): Ride {

        /*
         * Defensive protection.
         *
         * UI should not expose START while RECORDING.
         * Controller also prevents creation of another ride.
         */
        val existingRide =
            getCurrentRide()

        if (
            existingRide != null &&
            existingRide.ride.status ==
            RideStatus.RECORDING
        ) {

            RoadPulseLogger.ride(
                "START IGNORED | active=${existingRide.ride.rideId}"
            )

            return existingRide
        }


        return rideManager.startRide(
            rideMode = currentMode,
            vehicleType = vehicleType
        )
    }


    // =========================================================
    // STOP RIDE
    // =========================================================

    fun stopRide() {

        if (getRideStatus() != RideStatus.RECORDING) {
            return
        }


        val modeAtStop =
            currentMode


        rideManager.stopRide(
            StopReason.MANUAL
        )


        /*
         * User explicitly stopped an AUTO ride.
         *
         * AUTO mode therefore changes to MANUAL so that the
         * Auto Start logic cannot immediately start another ride.
         */
        if (modeAtStop == RideMode.AUTO) {

            currentMode =
                RideMode.MANUAL


            RoadPulseLogger.ui(
                "AUTO STOP | mode changed to MANUAL"
            )
        }
    }


    // =========================================================
    // GPS
    // =========================================================

    fun recordGps(
        record: GpsRecord
    ) {

        rideManager.recordGps(record)
    }
}