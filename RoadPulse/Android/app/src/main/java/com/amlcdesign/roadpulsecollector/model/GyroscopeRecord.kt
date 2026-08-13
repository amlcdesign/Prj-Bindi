package com.amlcdesign.roadpulsecollector.model

data class GyroscopeRecord(
    val timestampEpoch: Long,
    val timestampIso: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val samplingIntervalMs: Long
)