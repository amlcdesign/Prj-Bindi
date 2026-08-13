package com.amlcdesign.roadpulsecollector.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amlcdesign.roadpulsecollector.model.HistoryEntry
import com.amlcdesign.roadpulsecollector.storage.HistoryManager

@Composable
fun HistoryScreen(
    historyManager: HistoryManager,
    modifier: Modifier = Modifier
) {

    val history = remember {
        historyManager.getHistory()
    }

    if (history.isEmpty()) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No rides yet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        items(
            items = history.asReversed(),
            key = { it.rideUuid }
        ) { entry ->

            HistoryRideCard(
                entry = entry
            )
        }
    }
}


@Composable
private fun HistoryRideCard(
    entry: HistoryEntry
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = entry.rideId,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = entry.startIso,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Duration: ${formatDuration(entry.durationSeconds)}"
                )

                Text(
                    text = entry.rideMode.name
                )

                Text(
                    text = entry.vehicleType.name
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "GPS: ${entry.gpsPoints}"
                )

                Text(
                    text = "ACC: ${entry.accelerometerSamples}"
                )

                Text(
                    text = "GYRO: ${entry.gyroscopeSamples}"
                )
            }

            Text(
                text = "Status: ${entry.status.name}",
                style = MaterialTheme.typography.labelLarge
            )

            if (entry.submissionStatus != "NOT_SUBMITTED") {

                Text(
                    text = "Submission: ${entry.submissionStatus}"
                )
            }

            if (entry.processingStatus != "NOT_PROCESSED") {

                Text(
                    text = "Processing: ${entry.processingStatus}"
                )
            }

            if (entry.rewardPoints > 0) {

                Text(
                    text = "Reward: ${entry.rewardPoints} points"
                )
            }
        }
    }
}


private fun formatDuration(
    seconds: Int
): String {

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {

        hours > 0 ->
            String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                remainingSeconds
            )

        else ->
            String.format(
                "%02d:%02d",
                minutes,
                remainingSeconds
            )
    }
}