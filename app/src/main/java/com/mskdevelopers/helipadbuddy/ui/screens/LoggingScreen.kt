package com.mskdevelopers.helipadbuddy.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.local.FlightSession
import java.io.File
import com.mskdevelopers.helipadbuddy.ui.components.DisclaimerBanner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoggingScreen(
    sessions: List<FlightSession>,
    activeSessionId: Long?,
    exportResult: Result<File>?,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    onExportSession: (Long, Context) -> Unit,
    onClearExportResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        DisclaimerBanner()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartSession,
                enabled = activeSessionId == null
            ) {
                Text("Start session")
            }
            Button(
                onClick = onStopSession,
                enabled = activeSessionId != null
            ) {
                Text("Stop session")
            }
        }
        if (activeSessionId != null) {
            Text(
                "Recording session $activeSessionId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        exportResult?.let { result ->
            if (result.isSuccess) {
                Text(
                    "Exported: ${result.getOrNull()?.path}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    "Export failed: ${result.exceptionOrNull()?.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        Text(
            "Sessions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(sessions) { session ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!session.isActive) {
                                onExportSession(session.id, context)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Session ${session.id}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                dateFormat.format(Date(session.startTimeMillis)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (session.isActive) {
                                Text(
                                    "Recording",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (!session.isActive) {
                            Button(onClick = { onExportSession(session.id, context) }) {
                                Text("Export CSV")
                            }
                        }
                    }
                }
            }
        }
    }
}
