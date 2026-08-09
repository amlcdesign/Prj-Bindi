package com.amlcdesign.roadpulsecollector.manager

import android.os.Handler
import android.os.Looper
import com.amlcdesign.roadpulsecollector.model.GpsRecord

class AutoRideMonitor(
    private val onRideStart: () -> Unit
) {

    private val handler =
        Handler(Looper.getMainLooper())

    private var candidateActive = false

    private val startSpeedKmh = 10.0

    private val confirmationTimeMillis = 5000L

    private val confirmationRunnable = Runnable {

        if (candidateActive) {

            candidateActive = false

            onRideStart()
        }
    }

    fun processLocation(record: GpsRecord) {

        val speed = record.speedKmh

        if (speed > startSpeedKmh) {

            if (!candidateActive) {

                candidateActive = true

                handler.postDelayed(
                    confirmationRunnable,
                    confirmationTimeMillis
                )
            }

        } else {

            cancelCandidate()
        }
    }

    fun stop() {

        cancelCandidate()
    }

    private fun cancelCandidate() {

        candidateActive = false

        handler.removeCallbacks(
            confirmationRunnable
        )
    }
}