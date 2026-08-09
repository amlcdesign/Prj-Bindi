package com.amlcdesign.roadpulsecollector.model

data class EventRecord(

    val timestampEpoch: Long,

    val timestampIso: String,

    val eventType: String,

    val eventValue: String = ""
)