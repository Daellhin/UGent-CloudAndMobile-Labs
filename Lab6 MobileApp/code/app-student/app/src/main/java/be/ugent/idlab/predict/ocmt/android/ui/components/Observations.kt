package be.ugent.idlab.predict.ocmt.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import be.ugent.idlab.predict.ocmt.android.ui.format
import be.ugent.idlab.predict.ocmt.android.ui.graph.LineGraph
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun <Y> ObservedCountsWidget(
    flows: List<Flow<List<Pair<Instant, Y>>>>,
    modifier: Modifier = Modifier,
    verticalLabel: @Composable (Y) -> Unit = { Text(text = "${it.format(1)}$°C", maxLines = 1) }
) where Y: Number, Y: Comparable<Y> {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Visualised",
            style = MaterialTheme.typography.labelLarge
        )
        ObservationGraph(
            flows = flows,
            verticalLabel = verticalLabel
        )
    }
}

@Composable
private fun <Y> ObservationGraph(
    flows: List<Flow<List<Pair<Instant, Y>>>>,
    verticalLabel: @Composable (Y) -> Unit = { Text(text = "${it.format(1)}$°C", maxLines = 1) },
) where Y: Number, Y: Comparable<Y> {
    val data = flows.collectAsState()
    LineGraph(
        data = data.toList(),
        verticalLabel = verticalLabel,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 4.dp,
                vertical = 12.dp
            )
            .height(200.dp)
    )
}

@Composable
private fun <T> List<Flow<List<T>>>.collectAsState(): SnapshotStateList<List<T>> {
    val result = remember(this) {
        SnapshotStateList<List<T>>()
            .also { repeat(size) { _ -> it.add(emptyList()) } }
    }
    LaunchedEffect(key1 = this) {
        forEachIndexed { i, flow ->
            launch { flow.collect { collected -> result[i] = collected } }
        }
    }
    return result
}
