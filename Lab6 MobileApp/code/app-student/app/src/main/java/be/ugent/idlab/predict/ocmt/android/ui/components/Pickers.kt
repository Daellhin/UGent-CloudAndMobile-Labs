package be.ugent.idlab.predict.ocmt.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInput(onChange: (Instant?) -> Unit, textContent: @Composable (String?) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true
    )
    var datePickerState = rememberDatePickerState()

    var value by remember { mutableStateOf<Instant?>(null) }

    if (showDatePicker) {
        DatePickerModal(
            onConfirm = { newDatePickerState ->
                datePickerState = newDatePickerState
                showDatePicker = false
                showTimePicker = true
            },
            onDismiss = { showDatePicker = false },
            datePickerState
        )
    }
    if (showTimePicker) {
        TimePickerModal(
            onConfirm = { newTimePickerState ->
                value = datePickerState.selectedDateMillis?.let { dateMillis ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = dateMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    Instant.fromEpochMilliseconds(calendar.timeInMillis)
                }
                onChange(value)
                timePickerState = newTimePickerState
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            timePickerState
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val formattedStart = value?.let {
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            formatter.format(it.toEpochMilliseconds())
        }
        textContent(formattedStart)
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Edit")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
    initialTimePickerState: TimePickerState? = null
) {
    val timePickerState = initialTimePickerState ?: rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState) }) {
                Text("OK")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onConfirm: (DatePickerState) -> Unit,
    onDismiss: () -> Unit,
    initialDatePickerState: DatePickerState? = null
) {
    val datePickerState = initialDatePickerState ?: rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(datePickerState)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}