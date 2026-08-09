package com.amlcdesign.roadpulsecollector.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.amlcdesign.roadpulsecollector.enums.SensorSamplingProfile
import com.amlcdesign.roadpulsecollector.model.AccelerometerRecord
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class AccelerometerManager(
    context: Context,
    private val onSample: (AccelerometerRecord) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )

    private var samplingIntervalMs =
        SensorSamplingProfile.LOW_SPEED.intervalMs

    private var lastSampleTimestamp = 0L

    fun updateSpeed(speedKmh: Float) {

        samplingIntervalMs =
            SensorSamplingProfile
                .forSpeed(speedKmh)
                .intervalMs
    }

    fun start() {

        lastSampleTimestamp = 0L

        accelerometer?.let { sensor ->

            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {

        sensorManager.unregisterListener(this)

        lastSampleTimestamp = 0L
    }

    override fun onSensorChanged(
        event: SensorEvent
    ) {

        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        val now =
            System.currentTimeMillis()

        if (
            lastSampleTimestamp != 0L &&
            now - lastSampleTimestamp < samplingIntervalMs
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
            AccelerometerRecord(

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
        // Not required for current data collection.
    }
}