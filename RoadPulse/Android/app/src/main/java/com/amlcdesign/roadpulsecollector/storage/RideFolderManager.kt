package com.amlcdesign.roadpulsecollector.storage

import android.content.Context
import com.amlcdesign.roadpulsecollector.model.Ride
import com.google.gson.Gson
import java.io.File
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger

class RideFolderManager(
    private val context: Context
) {

    fun createRideFolder(ride: Ride): File {

        RoadPulseLogger.storage(
            "Ride folder created: ${ride.ride.rideId}"
        )

        val root = File(
            context.getExternalFilesDir(null),
            "RoadPulse"
        )

        if (!root.exists())
            root.mkdirs()

        val folder = File(
            root,
            ride.ride.rideId
        )

        if (!folder.exists())
            folder.mkdirs()

        val json = Gson().toJson(ride)

        File(folder, "ride.json")
            .writeText(json)

        return folder
    }

    fun updateRide(ride: Ride) {

        val root = File(
            context.getExternalFilesDir(null),
            "RoadPulse"
        )

        val folder = File(
            root,
            ride.ride.rideId
        )

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val json = Gson().toJson(ride)

        File(
            folder,
            "ride.json"
        ).writeText(json)
        RoadPulseLogger.storage(
            "ride.json updated: ${ride.ride.rideId}"
        )
    }

    fun getUnfinishedRide(): Ride? {

        val root = File(
            context.getExternalFilesDir(null),
            "RoadPulse"
        )

        if (!root.exists()) {
            return null
        }

        val folders = root.listFiles()
            ?: return null

        val gson = Gson()

        for (folder in folders) {

            if (!folder.isDirectory) {
                continue
            }

            val rideFile = File(
                folder,
                "ride.json"
            )

            if (!rideFile.exists()) {
                continue
            }

            try {

                val ride =
                    gson.fromJson(
                        rideFile.readText(),
                        Ride::class.java
                    )

                if (ride.ride.status == RideStatus.RECORDING) {

                    RoadPulseLogger.storage(
                        "Unfinished ride found: ${ride.ride.rideId}"
                    )

                    return ride
                }

            } catch (e: Exception) {

                RoadPulseLogger.storage(
                    "Unable to read ride.json: ${rideFile.absolutePath}"
                )
            }
        }

        return null
    }

}