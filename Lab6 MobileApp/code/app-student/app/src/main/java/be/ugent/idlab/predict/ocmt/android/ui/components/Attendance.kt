package be.ugent.idlab.predict.ocmt.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import be.ugent.idlab.predict.ocmt.android.data.service.AttendanceService.Response.AttendanceEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun formatTimeAgo(timestamp: Long): String {
    val eventTime = Instant.fromEpochMilliseconds(timestamp)
    val now = System.now()
    val duration = now - eventTime

    return when {
        duration.inWholeSeconds < 60 -> "${duration.inWholeSeconds} seconds ago"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes} minutes ago"
        duration.inWholeHours < 24 -> "${duration.inWholeHours} hours ago"
        else -> "${duration.inWholeDays} days ago"
    }
}

@Composable
fun AttendanceWidget(
    flow: Flow<List<AttendanceEvent>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Changes ",
            style = MaterialTheme.typography.labelLarge
        )

        val data = flow.collectAsState(initial = emptyList())
        data.value.forEach { event ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Icon(
                    imageVector = if (event.arrival) Icons.Default.GroupAdd else Icons.Default.GroupRemove,
                    contentDescription = "Observation Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = event.id,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimeAgo(event.timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}