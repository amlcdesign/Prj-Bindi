package com.amlcdesign.roadpulsecollector.storage

import com.amlcdesign.roadpulsecollector.model.AccelerometerRecord
import java.io.File

class AccelerometerCsvWriter(
    private val file: File
) {

    fun initialize() {

        if (!file.exists()) {

            file.parentFile?.mkdirs()

            file.writeText(
                "timestampEpoch,timestampIso,x,y,z,samplingIntervalMs\n"
            )
        }
    }

    fun write(record: AccelerometerRecord) {

        file.appendText(
            "${record.timestampEpoch}," +
                    "${record.timestampIso}," +
                    "${record.x}," +
                    "${record.y}," +
                    "${record.z}," +
                    "${record.samplingIntervalMs}\n"
        )
    }
}