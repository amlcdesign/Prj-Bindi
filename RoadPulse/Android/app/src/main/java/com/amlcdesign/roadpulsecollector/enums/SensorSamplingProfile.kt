package com.amlcdesign.roadpulsecollector.enums

//single configuration point for our dynamic sensor sampling.
//0-9.9 km/h -> LOW_SPEED
//10.0 km/h → CITY_SPEED
//40.0 km/h → HIGH_SPEED
//80.0 km/h → VERY_HIGH_SPEED

enum class SensorSamplingProfile(
    val minSpeedKmh: Float,
    val maxSpeedKmh: Float?,
    val intervalMs: Long
) {

    LOW_SPEED(
        minSpeedKmh = 0f,
        maxSpeedKmh = 10f,
        intervalMs = 200L
    ),

    CITY_SPEED(
        minSpeedKmh = 10f,
        maxSpeedKmh = 40f,
        intervalMs = 100L
    ),

    HIGH_SPEED(
        minSpeedKmh = 40f,
        maxSpeedKmh = 80f,
        intervalMs = 75L
    ),

    VERY_HIGH_SPEED(
        minSpeedKmh = 80f,
        maxSpeedKmh = null,
        intervalMs = 50L
    );

    companion object {

        fun forSpeed(speedKmh: Float): SensorSamplingProfile {

            return when {

                speedKmh < 10f ->
                    LOW_SPEED

                speedKmh < 40f ->
                    CITY_SPEED

                speedKmh < 80f ->
                    HIGH_SPEED

                else ->
                    VERY_HIGH_SPEED
            }
        }
    }
}