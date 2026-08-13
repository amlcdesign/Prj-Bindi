package com.amlcdesign.roadpulsecollector.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.amlcdesign.roadpulsecollector.manager.RideController

class RideControllerViewModel(
    application: Application
) : AndroidViewModel(application) {

    val rideController: RideController =
        RideController(application.applicationContext)
}