package com.amlcdesign.roadpulsecollector.model

data class GpsRecord(
    val timestampEpoch: Long,
    val timestampIso: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedKmh: Float,
    val altitudeMeters: Double,
    val bearingDegrees: Float
)