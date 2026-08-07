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
import com.amlcdesign.roadpulsecollector.manager.RideManager

@Composable
fun HomeScreen() {

    val context = LocalContext.current

    val rideManager = remember {
        RideManager(context)
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

    val gpsManager = remember {

        GpsManager(
            context = context
        ) { record ->

            rideManager.recordGps(record)

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
                modifier = Modifier.height(12.dp)
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

                    val ride =
                        rideManager.startRide()

                    rideId = ride.rideId

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

                    rideManager.stopRide()

                    isRecording = false
                }

            ) {

                Text("STOP RIDE")
            }
        }
    }
}