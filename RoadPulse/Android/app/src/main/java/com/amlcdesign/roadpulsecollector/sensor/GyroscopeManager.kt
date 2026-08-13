package com.amlcdesign.roadpulsecollector.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.amlcdesign.roadpulsecollector.model.GyroscopeRecord
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import com.amlcdesign.roadpulsecollector.utils.RoadPulseLogger

class GyroscopeManager(
    context: Context,
    private val onSample: (GyroscopeRecord) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager

    private val gyroscope =
        sensorManager.getDefaultSensor(
            Sensor.TYPE_GYROSCOPE
        )

    /*
     * Raw gyro collection only.
     *
     * Processing will be implemented later.
     */
    private val samplingIntervalMs = 50L

    private var lastSampleTimestamp = 0L
    private var isRunning = false

    fun start() {

        if (isRunning) {
            return
        }

        if (gyroscope == null) {

            RoadPulseLogger.gyro(
                "Gyroscope sensor not available"
            )

            return
        }

        isRunning = true
        lastSampleTimestamp = 0L

        gyroscope?.let { sensor ->

            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {

        if (!isRunning) {
            return
        }

        isRunning = false
        sensorManager.unregisterListener(this)

        lastSampleTimestamp = 0L


        RoadPulseLogger.gyro(
            "Gyroscope stopped"
        )
    }

    override fun onSensorChanged(
        event: SensorEvent
    ) {

        if (!isRunning) {
            return
        }

        if (
            event.sensor.type !=
            Sensor.TYPE_GYROSCOPE
        ) {
            return
        }

        val now =
            System.currentTimeMillis()

        if (
            lastSampleTimestamp != 0L &&
            now - lastSampleTimestamp <
            samplingIntervalMs
        ) {
            return
        }

        lastSampleTimestamp = now

        val timestampIso =
            OffsetDateTime.now()
                .format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )

        onSample(
            GyroscopeRecord(
                timestampEpoch = now,
                timestampIso = timestampIso,
                x = event.values[0],
                y = event.values[1],
                z = event.values[2],
                samplingIntervalMs =
                    samplingIntervalMs
            )
        )
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Not required for current raw data collection.
    }
}