package com.amlcdesign.roadpulsecollector.storage

import android.content.Context
import java.io.File

class RideFolderManager(
    private val context: Context
) {

    fun createRideFolder(
        rideId: String
    ): File {

        val root = File(
            context.getExternalFilesDir(null),
            "RoadPulse"
        )

        if (!root.exists())
            root.mkdirs()

        val rideFolder = File(
            root,
            rideId
        )

        if (!rideFolder.exists())
            rideFolder.mkdirs()

        return rideFolder
    }
}