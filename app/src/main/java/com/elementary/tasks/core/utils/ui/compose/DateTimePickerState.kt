package com.elementary.tasks.core.utils.ui.compose

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

private const val MILLIS_PER_DAY = 86_400_000L

interface DateTimePicker {
  fun showDatePicker(
    date: LocalDate,
    title: String,
    onDateSelected: (LocalDate) -> Unit,
  )

  fun showTimePicker(
    time: LocalTime,
    title: String,
    is24Hour: Boolean = true,
    onTimeSelected: (LocalTime) -> Unit,
  )
}

@Composable
fun rememberDateTimePicker(): DateTimePicker {
  val dateRequest = remember { mutableStateOf<DatePickerRequest?>(null) }
  val timeRequest = remember { mutableStateOf<TimePickerRequest?>(null) }

  dateRequest.value?.let {
    DatePickerRequestDialog(it, onDismiss = { dateRequest.value = null })
  }
  timeRequest.value?.let {
    TimePickerRequestDialog(it, onDismiss = { timeRequest.value = null })
  }

  return object : DateTimePicker {
    override fun showDatePicker(
      date: LocalDate,
      title: String,
      onDateSelected: (LocalDate) -> Unit
    ) {
      dateRequest.value = DatePickerRequest(date, title, onDateSelected)
    }

    override fun showTimePicker(
      time: LocalTime,
      title: String,
      is24Hour: Boolean,
      onTimeSelected: (LocalTime) -> Unit
    ) {
      timeRequest.value = TimePickerRequest(time, title, is24Hour, onTimeSelected)
    }
  }
}

private class DatePickerRequest(
  val initialDate: LocalDate,
  val title: String,
  val onDateSelected: (LocalDate) -> Unit,
)

private class TimePickerRequest(
  val initialTime: LocalTime,
  val title: String,
  val is24Hour: Boolean = true,
  val onTimeSelected: (LocalTime) -> Unit,
)

@Composable
private fun DatePickerRequestDialog(
  request: DatePickerRequest,
  onDismiss: () -> Unit,
) {
  val datePickerState =
    rememberDatePickerState(initialSelectedDateMillis = request.initialDate.toUtcMillis())
  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = {
        val millis = datePickerState.selectedDateMillis
        onDismiss()
        if (millis != null) request.onDateSelected(millis.toLocalDate())
      }) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    },
  ) {
    DatePicker(state = datePickerState, title = { Text(request.title) })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRequestDialog(
  request: TimePickerRequest,
  onDismiss: () -> Unit,
) {
  val timePickerState =
    rememberTimePickerState(
      initialHour = request.initialTime.hour,
      initialMinute = request.initialTime.minute,
      is24Hour = request.is24Hour,
    )
  TimePickerDialog(
    onDismissRequest = onDismiss,
    title = { Text(request.title) },
    confirmButton = {
      TextButton(onClick = {
        onDismiss()
        request.onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
      }) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    },
  ) {
    TimePicker(state = timePickerState)
  }
}

private fun LocalDate.toUtcMillis(): Long = toEpochDay() * MILLIS_PER_DAY

private fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this / MILLIS_PER_DAY)
