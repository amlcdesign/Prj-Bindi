package com.amlcdesign.roadpulsecollector.model

data class Ride(
    val rideId: String,
    val startTime: Long,
    var endTime: Long = 0L,
    var isRecording: Boolean = false
)