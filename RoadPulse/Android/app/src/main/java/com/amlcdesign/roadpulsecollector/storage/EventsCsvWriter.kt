package com.amlcdesign.roadpulsecollector.storage

import com.amlcdesign.roadpulsecollector.model.EventRecord
import java.io.File

class EventsCsvWriter(
    private val file: File
) {

    fun initialize() {

        if (!file.exists()) {

            file.parentFile?.mkdirs()

            file.writeText(
                "timestampEpoch,timestampIso,eventType,eventValue\n"
            )
        }
    }

    fun write(event: EventRecord) {

        file.appendText(
            "${event.timestampEpoch}," +
                    "${event.timestampIso}," +
                    "${event.eventType}," +
                    "${event.eventValue}\n"
        )
    }
}