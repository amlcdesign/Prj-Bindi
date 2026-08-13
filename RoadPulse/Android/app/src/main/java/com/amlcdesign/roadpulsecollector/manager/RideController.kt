package com.amlcdesign.roadpulsecollector.manager

import android.content.Context
import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.StopReason

import com.amlcdesign.roadpulsecollector.model.GpsRecord
import com.amlcdesign.roadpulsecollector.model.Ride
import com.amlcdesign.roadpulsecollector.enums.VehicleType
import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger
class RideController(
    context: Context
) {

    private val rideManager =
        RideManager(context)

    private var currentMode =
        RideMode.MANUAL

    private var onRideStarted: ((Ride) -> Unit)? = null

    fun setMode(mode: RideMode) {
        currentMode = mode
    }

    fun getMode(): RideMode {
        return currentMode
    }

    fun setOnRideStarted(
        callback: (Ride) -> Unit
    ) {
        onRideStarted = callback
    }

    init {
        RoadPulseLogger.ui(
            "RideController CREATED | hash=${hashCode()}"
        )
    }

    fun isRideActive(): Boolean {
        return rideManager.isRideActive()
    }

    fun getUnfinishedRide(): Ride? {
        return rideManager.getUnfinishedRide()
    }

    fun startRide(
        vehicleType: VehicleType = VehicleType.CAR
    ): Ride {

        val ride = rideManager.startRide(
            rideMode = currentMode,
            vehicleType = vehicleType
        )

//        rideManager.startAccelerometer()
//        rideManager.startGyroscope()
        onRideStarted?.invoke(ride)

        return ride
    }

    fun stopRide() {
//        rideManager.stopAccelerometer()
//        rideManager.stopGyroscope()
        rideManager.stopRide(
            StopReason.MANUAL
        )
    }


    fun recordGps(
        record: GpsRecord
    ) {
        rideManager.recordGps(record)
    }

    fun getCurrentRide(): Ride? {
        return rideManager.getCurrentRide()
    }
}