package com.amlcdesign.roadpulsecollector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {

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

            Spacer(modifier = Modifier.height(32.dp))

            Text("Status : Idle")

            Text("Vehicle : Car")

            Text("GPS : Waiting")

            Text("Sensors : Ready")

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { }
            ) {
                Text("START RIDE")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { }
            ) {
                Text("STOP RIDE")
            }
        }
    }
}