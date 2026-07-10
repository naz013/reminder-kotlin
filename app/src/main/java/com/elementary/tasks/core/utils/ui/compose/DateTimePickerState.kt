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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * Compose replacement for [com.elementary.tasks.core.utils.ui.DateTimePickerProvider]'s
 * `FragmentManager`-based `showDatePicker`/`showTimePicker`: queues a picker request that
 * [DateTimePickerDialogs] renders as a Material3 [DatePickerDialog]/[TimePickerDialog]. Needs no
 * Fragment/Activity/FragmentManager reference.
 *
 * Note: unlike [com.elementary.tasks.core.utils.ui.DateTimePickerProvider], the calendar's first
 * day of week always follows the platform locale — Material3's [DatePicker] has no public hook for
 * overriding it with the app's `Prefs.startDay` preference.
 */
@Stable
class DateTimePickerState internal constructor(
  internal val is24Hour: Boolean,
) {
  internal var datePickerRequest: DatePickerRequest? by mutableStateOf(null)
    private set

  internal var timePickerRequest: TimePickerRequest? by mutableStateOf(null)
    private set

  fun showDatePicker(
    date: LocalDate,
    title: String,
    onDateSelected: (LocalDate) -> Unit,
  ) {
    datePickerRequest = DatePickerRequest(date, title, onDateSelected)
  }

  fun showTimePicker(
    time: LocalTime,
    title: String,
    onTimeSelected: (LocalTime) -> Unit,
  ) {
    timePickerRequest = TimePickerRequest(time, title, onTimeSelected)
  }

  internal fun dismissDatePicker() {
    datePickerRequest = null
  }

  internal fun dismissTimePicker() {
    timePickerRequest = null
  }

  internal class DatePickerRequest(
    val initialDate: LocalDate,
    val title: String,
    val onDateSelected: (LocalDate) -> Unit,
  )

  internal class TimePickerRequest(
    val initialTime: LocalTime,
    val title: String,
    val onTimeSelected: (LocalTime) -> Unit,
  )
}

@Composable
fun rememberDateTimePickerState(is24Hour: Boolean): DateTimePickerState =
  remember(is24Hour) { DateTimePickerState(is24Hour) }

/** Renders whichever picker is currently requested on [state]; no-op while neither is pending. */
@Composable
fun DateTimePickerDialogs(state: DateTimePickerState) {
  state.datePickerRequest?.let { request -> DatePickerRequestDialog(request, onDismiss = state::dismissDatePicker) }
  state.timePickerRequest?.let { request ->
    TimePickerRequestDialog(request, is24Hour = state.is24Hour, onDismiss = state::dismissTimePicker)
  }
}

@Composable
private fun DatePickerRequestDialog(
  request: DateTimePickerState.DatePickerRequest,
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
  request: DateTimePickerState.TimePickerRequest,
  is24Hour: Boolean,
  onDismiss: () -> Unit,
) {
  val timePickerState =
    rememberTimePickerState(
      initialHour = request.initialTime.hour,
      initialMinute = request.initialTime.minute,
      is24Hour = is24Hour,
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
