package com.amlcdesign.roadpulsecollector.model

import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.enums.VehicleType


data class Ride(

    // ---------------------------------------------------------
    // Schema
    // ---------------------------------------------------------

    val schemaVersion: String = "1.0",

    // ---------------------------------------------------------
    // App information
    // ---------------------------------------------------------

    val app: AppInfo = AppInfo(),

    // ---------------------------------------------------------
    // User information
    // ---------------------------------------------------------

    val user: UserInfo = UserInfo(),

    // ---------------------------------------------------------
    // Device information
    // ---------------------------------------------------------

    val device: DeviceInfo = DeviceInfo(),

    // ---------------------------------------------------------
    // Vehicle information
    // ---------------------------------------------------------

    val vehicle: VehicleInfo = VehicleInfo(),

    // ---------------------------------------------------------
    // Ride information
    // ---------------------------------------------------------

    val ride: RideInfo,

    // ---------------------------------------------------------
    // Sensor collection configuration
    // ---------------------------------------------------------

    val collection: CollectionInfo = CollectionInfo(),

    // ---------------------------------------------------------
    // Collected data statistics
    // ---------------------------------------------------------

    var data: DataInfo = DataInfo(),

    // ---------------------------------------------------------
    // Submission information
    // ---------------------------------------------------------

    var submission: SubmissionInfo = SubmissionInfo(),

    // ---------------------------------------------------------
    // Processing information
    // ---------------------------------------------------------

    var processing: ProcessingInfo = ProcessingInfo(),

    // ---------------------------------------------------------
    // Reward information
    // ---------------------------------------------------------

    var reward: RewardInfo = RewardInfo()
)


// =============================================================
// APP
// =============================================================

data class AppInfo(

    val appName: String = "RoadPulse",

    val appVersion: String = "0.1.0",

    val platform: String = "ANDROID"
)


// =============================================================
// USER
// =============================================================

data class UserInfo(

    val userId: String = ""
)


// =============================================================
// DEVICE
// =============================================================

data class DeviceInfo(

    val deviceId: String = "",

    val manufacturer: String = "",

    val model: String = "",

    val os: String = "",

    val osVersion: String = ""
)


// =============================================================
// VEHICLE
// =============================================================

data class VehicleInfo(

    val vehicleType: VehicleType = VehicleType.CAR,

    val vehicleId: String = ""
)


// =============================================================
// RIDE
// =============================================================

data class RideInfo(

    // Human-readable ride ID
    val rideId: String,

    // Permanent unique ride ID
    val rideUuid: String,

    // Ride mode
    val rideMode: RideMode = RideMode.MANUAL,

    // Ride lifecycle status
    var status: RideStatus,

    // Why the ride started
    var startReason: String = "",

    // Why the ride stopped
    var stopReason: String = "",

    // Start time
    val startEpoch: Long,

    val startIso: String,

    // End time
    var endEpoch: Long? = null,

    var endIso: String? = null,

    // Duration
    var durationSeconds: Int = 0
)


// =============================================================
// COLLECTION
// =============================================================

data class CollectionInfo(

    val gpsIntervalMs: Long = 1000,

    val accelerometerIntervalMs: Long = 50,

    val gyroscopeIntervalMs: Long = 50
)


// =============================================================
// DATA STATISTICS
// =============================================================

data class DataInfo(

    var gpsPoints: Int = 0,

    var accelerometerSamples: Int = 0,

    var gyroscopeSamples: Int = 0,

    var eventCount: Int = 0
)


// =============================================================
// SUBMISSION
// =============================================================

data class SubmissionInfo(

    var status: String = "NOT_SUBMITTED",

    var submittedAtEpoch: Long = 0,

    var submittedAtIso: String = "",

    var serverRideId: String = ""
)


// =============================================================
// PROCESSING
// =============================================================

data class ProcessingInfo(

    var status: String = "NOT_PROCESSED",

    var processedAtEpoch: Long = 0,

    var processedAtIso: String = "",

    var qualityScore: Int = 0,

    var accepted: Boolean = false
)


// =============================================================
// REWARD
// =============================================================

data class RewardInfo(

    var points: Int = 0,

    var basePoints: Int = 0,

    var bonusPoints: Int = 0,

    var rewardStatus: String = "PENDING"
)