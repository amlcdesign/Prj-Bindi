package com.amlcdesign.roadpulsecollector.storage

import android.content.Context
import com.amlcdesign.roadpulsecollector.model.Ride
import com.google.gson.Gson
import java.io.File

class RideFolderManager(
    private val context: Context
) {

    fun createRideFolder(ride: Ride): File {

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
    }
}