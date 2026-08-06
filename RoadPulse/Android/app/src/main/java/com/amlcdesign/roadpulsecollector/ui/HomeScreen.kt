package com.amlcdesign.roadpulsecollector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amlcdesign.roadpulsecollector.manager.RideManager
import androidx.compose.ui.platform.LocalContext


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

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "RoadPulse",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text =
                    if (isRecording)
                        "🔴 Recording"
                    else
                        "🟢 Idle"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Ride ID")

            Text(rideId)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                enabled = !isRecording,

                onClick = {

                    val ride =
                        rideManager.startRide()

                    rideId = ride.rideId

                    isRecording = true

                }
            ) {

                Text("START RIDE")

            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(

                enabled = isRecording,

                onClick = {

                    rideManager.stopRide()

                    isRecording = false

                }

            ) {

                Text("STOP RIDE")

            }

        }

    }

}