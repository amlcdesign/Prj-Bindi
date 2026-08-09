package com.amlcdesign.roadpulsecollector.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.amlcdesign.roadpulsecollector.gps.GpsManager
import com.amlcdesign.roadpulsecollector.manager.RideController
import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.manager.AutoRideMonitor
import com.amlcdesign.roadpulsecollector.enums.VehicleType

@Composable
fun HomeScreen() {

    val context = LocalContext.current

    val rideController = remember {
        RideController(context)
    }



    var isRecording by remember {
        mutableStateOf(false)
    }

    var rideId by remember {
        mutableStateOf("")
    }

    var latitude by remember {
        mutableStateOf("--")
    }

    var longitude by remember {
        mutableStateOf("--")
    }

    var speed by remember {
        mutableStateOf("--")
    }

    var accuracy by remember {
        mutableStateOf("--")
    }

    var rideMode by remember {
        mutableStateOf(RideMode.MANUAL)
    }

    var vehicleType by remember {
        mutableStateOf(VehicleType.CAR)
    }

    //RideController can automatically start the ride,
    // but HomeScreen doesn't know that happened.
    // we need to tell HomeScreen when the ride starts.
    LaunchedEffect(Unit) {

        rideController.setOnRideStarted { ride ->

            rideId = ride.ride.rideId

            isRecording = true
        }
    }

    val autoRideMonitor = remember {

        AutoRideMonitor {

            rideController.setMode(
                RideMode.AUTO
            )

            val ride = rideController.startRide()

            rideId = ride.ride.rideId

            isRecording = true
        }
    }

    val gpsManager = remember {

        GpsManager(
            context = context
        ) { record ->

            latitude =
                String.format(
                    "%.6f",
                    record.latitude
                )

            longitude =
                String.format(
                    "%.6f",
                    record.longitude
                )

            speed =
                String.format(
                    "%.1f km/h",
                    record.speedKmh
                )

            accuracy =
                String.format(
                    "%.1f m",
                    record.accuracyMeters
                )

            // AUTO MODE:
            // monitor movement before ride starts
            if (
                rideMode == RideMode.AUTO &&
                !isRecording
            ) {

                autoRideMonitor.processLocation(
                    record
                )
            }

            // Record GPS only after ride starts
            if (isRecording) {

                rideController.recordGps(
                    record
                )
            }
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseLocation =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (fineLocation || coarseLocation) {

                gpsManager.start()
            }
        }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Text(
                text = "RoadPulse",
                style =
                    MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text =
                    if (isRecording)
                        "🔴 Recording"
                    else
                        "🟢 Idle"
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Vehicle",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = vehicleType == VehicleType.CAR,
                    onClick = {
                        if (!isRecording) {
                            vehicleType = VehicleType.CAR
                        }
                    }
                )

                Text("Car")

                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                RadioButton(
                    selected = vehicleType == VehicleType.BIKE,
                    onClick = {
                        if (!isRecording) {
                            vehicleType = VehicleType.BIKE
                        }
                    }
                )

                Text("Bike")
            }

            Text(
                text = "Ride Mode",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // mode can only be changed while the app is idle.
                RadioButton(
                    selected = rideMode == RideMode.AUTO,
                    onClick = {

                        if (!isRecording) {

                            rideMode = RideMode.AUTO

                            val hasPermission =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                            if (rideMode == RideMode.MANUAL) {
                                if (hasPermission) {
                                    //if AUTO GPS is already started
                                    gpsManager.start()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }

                        }
                    }
                )

                Text("Auto Mode")

                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                RadioButton(
                    selected = rideMode == RideMode.MANUAL,
                    onClick = {

                        if (!isRecording) {
                            rideMode = RideMode.MANUAL
                            autoRideMonitor.stop()
                            gpsManager.stop()
                        }
                    }
                )

                Text("Manual Mode")
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text("Ride ID")

            Text(rideId)

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text("GPS")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text("Latitude : $latitude")

            Text("Longitude : $longitude")

            Text("Speed : $speed")

            Text("Accuracy : $accuracy")

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Button(

                enabled = !isRecording,

                onClick = {

                    val hasPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    rideController.setMode(rideMode)

                    val ride =
                        rideController.startRide(
                            vehicleType = vehicleType
                        )

                    rideId = ride.ride.rideId

                    isRecording = true

                    if (hasPermission) {

                        gpsManager.start()

                    } else {

                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

            ) {

                Text("START RIDE")
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedButton(

                enabled = isRecording,

                onClick = {

                    gpsManager.stop()

                    rideController.stopRide()

                    isRecording = false
                }

            ) {

                Text("STOP RIDE")
            }
        }
    }
}