package com.amlcdesign.roadpulsecollector.storage

import android.content.Context
import com.amlcdesign.roadpulsecollector.model.HistoryEntry
import com.amlcdesign.roadpulsecollector.model.Ride
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class HistoryManager(
    private val context: Context
) {

    private val gson = Gson()

    private val root: File
        get() = File(
            context.getExternalFilesDir(null),
            "RoadPulse"
        )

    private val historyFile: File
        get() = File(
            root,
            "history.json"
        )


    // =========================================================
    // CREATE
    // =========================================================

    fun createHistoryEntry(ride: Ride) {

        ensureRoot()

        val history = getHistory().toMutableList()

        /*
         * At most one active ride is allowed.
         * Therefore there should never be more than one
         * RECORDING HistoryEntry.
         *
         * Do not create a duplicate entry if the same
         * ride is already present.
         */
        if (
            history.any {
                it.rideUuid == ride.ride.rideUuid
            }
        ) {
            return
        }

        val entry = HistoryEntry(

            rideId =
                ride.ride.rideId,

            rideUuid =
                ride.ride.rideUuid,

            status =
                ride.ride.status,

            rideMode =
                ride.ride.rideMode,

            vehicleType =
                ride.vehicle.vehicleType,

            startEpoch =
                ride.ride.startEpoch,

            startIso =
                ride.ride.startIso
        )

        history.add(entry)

        saveHistory(history)
    }


    // =========================================================
    // UPDATE
    // =========================================================

    fun updateHistoryEntry(ride: Ride) {

        ensureRoot()

        val history = getHistory().toMutableList()

        val index =
            history.indexOfFirst {
                it.rideUuid == ride.ride.rideUuid
            }

        /*
         * If the History entry does not exist, create it.
         *
         * This also makes the method safe during recovery
         * or when older rides do not have a History entry.
         */
        if (index < 0) {

            createHistoryEntry(ride)

            return
        }

        val entry =
            history[index]

        // -----------------------------------------------------
        // Ride
        // -----------------------------------------------------

        entry.status =
            ride.ride.status

        entry.endEpoch =
            ride.ride.endEpoch

        entry.endIso =
            ride.ride.endIso

        entry.durationSeconds =
            ride.ride.durationSeconds

        // -----------------------------------------------------
        // Data
        // -----------------------------------------------------

        entry.gpsPoints =
            ride.data.gpsPoints

        entry.accelerometerSamples =
            ride.data.accelerometerSamples

        entry.gyroscopeSamples =
            ride.data.gyroscopeSamples

        entry.eventCount =
            ride.data.eventCount

        // -----------------------------------------------------
        // Submission
        // -----------------------------------------------------

        entry.submissionStatus =
            ride.submission.status

        entry.submittedAtEpoch =
            ride.submission.submittedAtEpoch

        entry.submittedAtIso =
            ride.submission.submittedAtIso

        entry.serverRideId =
            ride.submission.serverRideId

        // -----------------------------------------------------
        // Processing
        // -----------------------------------------------------

        entry.processingStatus =
            ride.processing.status

        entry.processedAtEpoch =
            ride.processing.processedAtEpoch

        entry.processedAtIso =
            ride.processing.processedAtIso

        entry.qualityScore =
            ride.processing.qualityScore

        entry.accepted =
            ride.processing.accepted

        // -----------------------------------------------------
        // Reward
        // -----------------------------------------------------

        entry.rewardPoints =
            ride.reward.points

        entry.basePoints =
            ride.reward.basePoints

        entry.bonusPoints =
            ride.reward.bonusPoints

        entry.rewardStatus =
            ride.reward.rewardStatus

        saveHistory(history)
    }


    // =========================================================
    // READ
    // =========================================================

    fun getHistory(): List<HistoryEntry> {

        if (!historyFile.exists()) {
            return emptyList()
        }

        return try {

            val json =
                historyFile.readText()

            if (json.isBlank()) {
                emptyList()
            } else {

                val type =
                    object :
                        TypeToken<List<HistoryEntry>>() {}
                        .type

                gson.fromJson(
                    json,
                    type
                )
            }

        } catch (e: Exception) {

            emptyList()
        }
    }


    // =========================================================
    // CLEAR
    // =========================================================

    fun clearHistory() {

        if (historyFile.exists()) {
            historyFile.delete()
        }
    }


    // =========================================================
    // STORAGE
    // =========================================================

    private fun saveHistory(
        history: List<HistoryEntry>
    ) {

        ensureRoot()

        val json =
            gson.toJson(history)

        historyFile.writeText(json)
    }


    private fun ensureRoot() {

        if (!root.exists()) {
            root.mkdirs()
        }
    }
}