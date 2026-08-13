package com.amlcdesign.roadpulsecollector.gps


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.amlcdesign.roadpulsecollector.model.GpsRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger

class GpsManager(
    private val context: Context,
    private val onLocationUpdate: (GpsRecord) -> Unit
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        )
            .setMinUpdateIntervalMillis(500L)
            .setWaitForAccurateLocation(false)
            .build()

    private val locationCallback =
        object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {

                result.locations.forEach { location ->

                    val timestampEpoch =
                        location.time

                    val timestampIso =
                        DateTimeFormatter
                            .ISO_OFFSET_DATE_TIME
                            .withZone(
                                ZoneId.systemDefault()
                            )
                            .format(
                                Instant.ofEpochMilli(
                                    timestampEpoch
                                )
                            )

                    val speedKmh =
                        if (location.hasSpeed())
                            location.speed * 3.6f
                        else
                            0f

                    val altitude =
                        if (location.hasAltitude())
                            location.altitude
                        else
                            0.0

                    val bearing =
                        if (location.hasBearing())
                            location.bearing
                        else
                            0f

                    val record = GpsRecord(

                        timestampEpoch =
                            timestampEpoch,

                        timestampIso =
                            timestampIso,

                        latitude =
                            location.latitude,

                        longitude =
                            location.longitude,

                        accuracyMeters =
                            location.accuracy,

                        speedKmh =
                            speedKmh,

                        altitudeMeters =
                            altitude,

                        bearingDegrees =
                            bearing
                    )

                    onLocationUpdate(record)
                }
            }
        }

    @SuppressLint("MissingPermission")
    fun start() {

        RoadPulseLogger.gps(
            "GPS started"
        )
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stop() {

        RoadPulseLogger.gps(
            "GPS stopped"
        )
        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )
    }
}