package com.amlcdesign.roadpulsecollector.model

import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.enums.VehicleType

data class HistoryEntry(

    // ---------------------------------------------------------
    // Ride identification
    // ---------------------------------------------------------

    val rideId: String,

    val rideUuid: String,

    // ---------------------------------------------------------
    // Ride information
    // ---------------------------------------------------------

    var status: RideStatus,

    val rideMode: RideMode,

    val vehicleType: VehicleType,

    // ---------------------------------------------------------
    // Time information
    // ---------------------------------------------------------

    val startEpoch: Long,

    val startIso: String,

    var endEpoch: Long? = null,

    var endIso: String? = null,

    var durationSeconds: Int = 0,

    // ---------------------------------------------------------
    // Data statistics
    // ---------------------------------------------------------

    var gpsPoints: Int = 0,

    var accelerometerSamples: Int = 0,

    var gyroscopeSamples: Int = 0,

    var eventCount: Int = 0,

    // ---------------------------------------------------------
    // Submission
    // ---------------------------------------------------------

    var submissionStatus: String = "NOT_SUBMITTED",

    var submittedAtEpoch: Long = 0,

    var submittedAtIso: String = "",

    var serverRideId: String = "",

    // ---------------------------------------------------------
    // Processing
    // ---------------------------------------------------------

    var processingStatus: String = "NOT_PROCESSED",

    var processedAtEpoch: Long = 0,

    var processedAtIso: String = "",

    var qualityScore: Int = 0,

    var accepted: Boolean = false,

    // ---------------------------------------------------------
    // Reward
    // ---------------------------------------------------------

    var rewardPoints: Int = 0,

    var basePoints: Int = 0,

    var bonusPoints: Int = 0,

    var rewardStatus: String = "PENDING"
)