package be.ugent.idlab.predict.ocmt.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import be.ugent.idlab.predict.ocmt.android.ui.components.AttendanceWidget
import be.ugent.idlab.predict.ocmt.android.ui.components.DatePickerModal
import be.ugent.idlab.predict.ocmt.android.ui.components.ObservedCountsWidget
import be.ugent.idlab.predict.ocmt.android.ui.components.TimeInput
import be.ugent.idlab.predict.ocmt.android.ui.components.TimePickerModal
import be.ugent.idlab.predict.ocmt.android.ui.composition.LocalNavigation
import be.ugent.idlab.predict.ocmt.android.util.getAttendanceService
import be.ugent.idlab.predict.ocmt.android.util.getCountsService
import be.ugent.idlab.predict.ocmt.android.util.getForecastService
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// todo
// https://developer.android.com/develop/ui/views/components/pickers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreen(source: String) {
    val navigation = LocalNavigation.current
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
    ) {
        val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LargeTopAppBar(
            title = {
                Text("$source's observations")
            },
            navigationIcon = {
                IconButton(
                    onClick = { navigation().popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            scrollBehavior = topAppBarScrollBehavior
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())

        ) {
            var start by remember { mutableStateOf<Instant?>(null) }
            var end by remember { mutableStateOf<Instant?>(null) }
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                TimeInput(
                    onChange = { start = it },
                    textContent = { formattedText ->
                        Text(text = "Start: ${formattedText ?: "Now-30m"}")
                    }
                )
                TimeInput(
                    onChange = { end = it },
                    textContent = { formattedText ->
                        Text(text = "End: ${formattedText ?: "Now"}")
                    }
                )
            }

            /**
             * Graph configuration: show / hide forecasts
             */
            var showForecasts by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showForecasts) "Forecasts are being displayed" else "No forecasts are shown",
                    style = MaterialTheme.typography.labelLarge
                )
                TextButton(
                    onClick = { showForecasts = !showForecasts }
                ) {
                    val text = if (showForecasts) {
                        "Hide forecasts"
                    } else {
                        "Show forecasts"
                    }
                    Text(text)
                }
            }
            /**
             * The graph itself, showing either a single or two lines depending on the value above
             */
            // all counts/forecasts graph logic
            val countsService = getCountsService()
            val forecastService = getForecastService()
            val attendanceService = getAttendanceService()
            val countsData = remember(source, start, end) {
                countsService.observe(source, start, end)
                    .map { it.map { Instant.fromEpochMilliseconds(it.timestamp) to it.value } }
            }
            val forecastData = remember(source, start) {
                forecastService.observe(source, start)
                    .map { it.map { Instant.fromEpochMilliseconds(it.timestamp) to it.value } }
            }
            val attendanceData = remember(source, start, end) {
                attendanceService.observe(source, start, end)
            }

            ObservedCountsWidget(
                flows = if (showForecasts) listOf(countsData, forecastData) else listOf(countsData),
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalLabel = { Text(it.toString()) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            /**
             * The raw attendance overview widget, drawn below the Graph
             */
            AttendanceWidget(
                flow = attendanceData,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
