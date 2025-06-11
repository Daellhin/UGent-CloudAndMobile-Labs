package be.ugent.idlab.predict.ocmt.android.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.ugent.idlab.predict.ocmt.android.ui.format
import be.ugent.idlab.predict.ocmt.android.util.localised
import be.ugent.idlab.predict.ocmt.android.util.toCompactString
import kotlinx.datetime.Instant

@Composable
fun <Y> LineGraph(
    data: List<List<Pair<Instant, Y>>>,
    modifier: Modifier = Modifier,
    verticalLabel: @Composable (Y) -> Unit = { Text(text = "${it.format(1)}$°C", maxLines = 1) },
    lineWidth: Dp = 2.5.dp,
    lineColors: List<Color> =
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
) where Y: Comparable<Y>, Y: Number {
    val normalized = remember(data) { data.normalized() }
    LineGraph(
        data = normalized,
        modifier = modifier,
        verticalLabel = verticalLabel,
        lineWidth = lineWidth,
        lineColors = lineColors
    )
}

@Composable
fun <Y> LineGraph(
    data: NormalizedResult<Instant, Y>?,
    modifier: Modifier = Modifier,
    verticalLabel: @Composable (Y) -> Unit = { Text(text = "${it.format(1)}$°C", maxLines = 1) },
    lineWidth: Dp = 2.5.dp,
    lineColors: List<Color> =
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
) where Y: Comparable<Y>, Y: Number {
    val axesColor = MaterialTheme.colorScheme.onBackground
    val lineWidthPx = LocalDensity.current.run { lineWidth.toPx() }
    GraphLayout(
        modifier = modifier,
        verticalLabels = {
            data ?: return@GraphLayout
            verticalLabel(data.topRight.second)
            verticalLabel(data.bottomLeft.second)
        },
        horizontalLabels = {
            data ?: return@GraphLayout
            val start = data.bottomLeft.first.localised()
            val end = data.topRight.first.localised()
            val (first, second) = if (start.date != end.date) {
                start.toCompactString() to end.toCompactString()
            } else {
                start.time.toCompactString() to end.time.toCompactString()
            }
            Text(text = first)
            Text(text = second)
        }
    ) {
        data?.lines?.forEachIndexed { i, line ->
            GraphCanvasLine(
                modifier = Modifier.padding(lineWidth + 8.dp),
                line = line,
                lineWidth = lineWidthPx,
                lineColor = lineColors[i % lineColors.size]
            )
        }
        GraphCanvasAxes(
            modifier = Modifier.padding(lineWidth + 4.dp),
            lineWidth = lineWidthPx,
            axesColor = axesColor
        )
    }
}

@Composable
private fun GraphLayout(
    modifier: Modifier = Modifier,
    verticalLabels: @Composable ColumnScope.() -> Unit,
    horizontalLabels: @Composable RowScope.() -> Unit,
    graph: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.labelSmall
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .requiredWidth(40.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                    content = verticalLabels
                )
            }
            Box {
                graph()
            }
        }
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelSmall
        ) {
            Row(
                modifier = Modifier
                    .requiredHeight(LocalDensity.current.run { MaterialTheme.typography.labelSmall.lineHeight.toDp() })
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                content = horizontalLabels
            )
        }
    }

}

@Composable
private fun GraphCanvasLine(
    modifier: Modifier = Modifier,
    line: List<ScaledPoint>,
    lineWidth: Float = LocalDensity.current.run { 2.5.dp.toPx() },
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        var prior = line.first()
        line.forEach {
            val start = Offset(
                x = size.width * prior.x,
                y = size.height * (1 - prior.y)
            )
            val end = Offset(
                x = size.width * it.x,
                y = size.height * (1 - it.y)
            )
            drawCircle(
                color = lineColor,
                radius = lineWidth,
                center = end
            )
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = lineWidth
            )
            prior = it
        }
    }
}

@Composable
private fun GraphCanvasAxes(
    modifier: Modifier = Modifier,
    lineWidth: Float = LocalDensity.current.run { 2.5.dp.toPx() },
    axesColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        // the axes last, so they aren't interrupted by the graph itself
        drawLine(
            color = axesColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = lineWidth
        )
        drawLine(
            color = axesColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = lineWidth
        )
    }
}
