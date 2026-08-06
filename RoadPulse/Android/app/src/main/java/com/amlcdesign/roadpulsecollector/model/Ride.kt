package com.amlcdesign.roadpulsecollector.model
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.enums.VehicleType
data class Ride(

    // Human readable ID
    val rideId: String,

    // Permanent unique ID
    val rideUuid: String,

    // CREATED, RECORDING, COMPLETED, UPLOADED
    var status: RideStatus,

    // Time
    val startEpoch: Long,
    val startIso: String,

    var endEpoch: Long? = null,
    var endIso: String? = null,

    // Vehicle
    var vehicleType: VehicleType = VehicleType.CAR,

    // App
    var appVersion: String = "0.1.0",

    // Statistics
    var durationSeconds: Int = 0,
    var gpsPoints: Int = 0,
    var accelerometerSamples: Int = 0,
    var gyroscopeSamples: Int = 0
)