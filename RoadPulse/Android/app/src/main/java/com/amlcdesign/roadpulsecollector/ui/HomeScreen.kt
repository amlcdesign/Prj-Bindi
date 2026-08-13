package com.amlcdesign.roadpulsecollector.ui

import android.Manifest
import android.content.pm.PackageManager

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.core.content.ContextCompat

import com.amlcdesign.roadpulsecollector.enums.RideMode
import com.amlcdesign.roadpulsecollector.enums.RideStatus
import com.amlcdesign.roadpulsecollector.enums.VehicleType

import com.amlcdesign.roadpulsecollector.gps.GpsManager

import com.amlcdesign.roadpulsecollector.manager.AutoRideMonitor
import com.amlcdesign.roadpulsecollector.manager.RideController

import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    rideController: RideController
) {

    val context = LocalContext.current


    // =========================================================
    // RIDE STATE
    // =========================================================

    /*
     * This is only a Compose refresh trigger.
     *
     * It is NOT the source of truth for ride status.
     * The source of truth remains RideController/RideManager.
     */
    var rideStateVersion by remember {
        mutableIntStateOf(0)
    }


    /*
     * Always obtain the current ride state from Controller.
     */
    val rideStatus =
        remember(rideStateVersion) {
            rideController.getRideStatus()
        }


    val currentRide =
        remember(rideStateVersion) {
            rideController.getCurrentRide()
        }


    val isRecording =
        rideStatus == RideStatus.RECORDING


    val rideId =
        currentRide?.ride?.rideId ?: ""


    // =========================================================
    // LIVE GPS DATA
    // =========================================================

    /*
     * These remain UI state because they represent the latest
     * sensor observations.
     */
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


    // =========================================================
    // USER SETTINGS
    // =========================================================

    var rideMode by remember {
        mutableStateOf(RideMode.MANUAL)
    }

    var vehicleType by remember {
        mutableStateOf(VehicleType.CAR)
    }


    // =========================================================
    // LOG CONTROLLER INSTANCE
    // =========================================================

    RoadPulseLogger.ui(
        "HomeScreen RideController | hash=${rideController.hashCode()}"
    )


    // =========================================================
    // AUTO RIDE MONITOR
    // =========================================================

    val autoRideMonitor = remember {

        AutoRideMonitor {

            /*
             * Auto Start is only allowed when the application
             * is currently IDLE.
             */
            if (
                rideController.getRideStatus() ==
                RideStatus.IDLE
            ) {

                rideController.setMode(
                    RideMode.AUTO
                )

                val ride =
                    rideController.startRide(
                        vehicleType = vehicleType
                    )

                /*
                 * Controller is now authoritative.
                 *
                 * Trigger Compose to re-read the state.
                 */
                rideStateVersion++
            }
        }
    }


    // =========================================================
    // GPS MANAGER
    // =========================================================

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


            // -------------------------------------------------
            // AUTO MODE
            // -------------------------------------------------

            if (
                rideMode == RideMode.AUTO &&
                rideController.getRideStatus() ==
                RideStatus.IDLE
            ) {

                autoRideMonitor.processLocation(
                    record
                )
            }


            // -------------------------------------------------
            // RECORD GPS
            // -------------------------------------------------

            /*
             * Do not use a local isRecording flag as the
             * authority. Ask Controller.
             */
            if (
                rideController.getRideStatus() ==
                RideStatus.RECORDING
            ) {

                rideController.recordGps(
                    record
                )
            }
        }
    }


    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

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


            if (
                fineLocation ||
                coarseLocation
            ) {

                gpsManager.start()
            }
        }


    // =========================================================
    // UI
    // =========================================================

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.SpaceBetween
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


            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

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


            // -------------------------------------------------
            // VEHICLE
            // -------------------------------------------------

            Text(
                text = "Vehicle",
                style =
                    MaterialTheme.typography.titleMedium
            )


            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        vehicleType ==
                                VehicleType.CAR,

                    onClick = {

                        if (!isRecording) {

                            vehicleType =
                                VehicleType.CAR
                        }
                    }
                )

                Text("Car")


                Spacer(
                    modifier = Modifier.width(16.dp)
                )


                RadioButton(
                    selected =
                        vehicleType ==
                                VehicleType.BIKE,

                    onClick = {

                        if (!isRecording) {

                            vehicleType =
                                VehicleType.BIKE
                        }
                    }
                )

                Text("Bike")
            }


            // -------------------------------------------------
            // RIDE MODE
            // -------------------------------------------------

            Text(
                text = "Ride Mode",
                style =
                    MaterialTheme.typography.titleMedium
            )


            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        rideMode ==
                                RideMode.AUTO,

                    onClick = {

                        if (!isRecording) {

                            rideMode =
                                RideMode.AUTO

                            val hasPermission =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) ==
                                        PackageManager.PERMISSION_GRANTED

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
                    }
                )

                Text("Auto Mode")


                Spacer(
                    modifier = Modifier.width(16.dp)
                )


                RadioButton(
                    selected =
                        rideMode ==
                                RideMode.MANUAL,

                    onClick = {

                        if (!isRecording) {

                            rideMode =
                                RideMode.MANUAL

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


            // -------------------------------------------------
            // RIDE ID
            // -------------------------------------------------

            Text("Ride ID")

            Text(
                text = rideId
            )


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            HorizontalDivider()


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // -------------------------------------------------
            // GPS
            // -------------------------------------------------

            Text("GPS")


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(
                text = "Latitude : $latitude"
            )

            Text(
                text = "Longitude : $longitude"
            )

            Text(
                text = "Speed : $speed"
            )

            Text(
                text = "Accuracy : $accuracy"
            )


            Spacer(
                modifier = Modifier.height(32.dp)
            )


            // -------------------------------------------------
            // START / STOP
            // -------------------------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // -------------------------------------------------
                // START
                // -------------------------------------------------

                Button(
                    enabled = !isRecording,

                    onClick = {

                        val hasPermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) ==
                                    PackageManager.PERMISSION_GRANTED


                        rideController.setMode(
                            rideMode
                        )


                        rideController.startRide(
                            vehicleType =
                                vehicleType
                        )


                        /*
                         * Controller is authoritative.
                         * Trigger UI refresh.
                         */
                        rideStateVersion++


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


                // -------------------------------------------------
                // STOP
                // -------------------------------------------------

                Button(
                    enabled = isRecording,

                    onClick = {

                        gpsManager.stop()

                        rideController.stopRide()


                        /*
                         * RideManager now sets currentRide = null.
                         *
                         * Refresh UI from Controller so:
                         *
                         * Status → IDLE
                         * Ride ID → blank
                         */
                        rideStateVersion++
                    },

                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {

                    Text("STOP RIDE")
                }
            }
        }
    }
}