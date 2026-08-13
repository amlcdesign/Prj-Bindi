package com.amlcdesign.roadpulsecollector.storage

import com.amlcdesign.roadpulsecollector.model.GyroscopeRecord
import java.io.File

class GyroscopeCsvWriter(
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

    fun write(record: GyroscopeRecord) {

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