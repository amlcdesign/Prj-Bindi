package com.amlcdesign.roadpulsecollector.storage

import com.amlcdesign.roadpulsecollector.model.GpsRecord
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GpsCsvWriter(
    private val file: File
) {

    private var initialized = false

    private val formatter =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withZone(ZoneId.systemDefault())

    fun initialize() {

        if (!file.exists()) {

            file.writeText(
                "timestampEpoch,timestampIso," +
                        "latitude,longitude,accuracyMeters," +
                        "speedKmh,altitudeMeters,bearingDegrees\n"
            )
        }

        initialized = true
    }

    fun write(record: GpsRecord) {

        if (!initialized) {
            initialize()
        }

        val line =
            "${record.timestampEpoch}," +
                    "${record.timestampIso}," +
                    "${record.latitude}," +
                    "${record.longitude}," +
                    "${record.accuracyMeters}," +
                    "${record.speedKmh}," +
                    "${record.altitudeMeters}," +
                    "${record.bearingDegrees}\n"

        file.appendText(line)
    }
}