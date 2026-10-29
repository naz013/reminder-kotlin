package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Bottom sheet offering Today / Tomorrow / pick date and time as the target of a bulk reschedule.
 * Stays open until the caller dismisses it, so the picker dialogs keep their host composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleTargetSheet(
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
  onTodayClick: () -> Unit,
  onTomorrowClick: () -> Unit,
  onPicked: (LocalDateTime) -> Unit,
) {
  val dateTimePicker = rememberDateTimePicker()
  val selectDateTitle = stringResource(R.string.select_date)
  val selectTimeTitle = stringResource(R.string.select_time)

  AppModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismissRequest,
  ) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
      BottomSheetHeader(title = stringResource(R.string.reschedule_sheet_title))
      TargetRow(
        text = stringResource(R.string.today),
        icon = { Icon(painter = AppIcons.Fluent.CalendarDay, contentDescription = null) },
        onClick = onTodayClick,
      )
      TargetRow(
        text = stringResource(R.string.tomorrow),
        icon = { Icon(painter = AppIcons.Fluent.CalendarMonth, contentDescription = null) },
        onClick = onTomorrowClick,
      )
      TargetRow(
        text = stringResource(R.string.reschedule_pick_date),
        icon = { Icon(painter = AppIcons.Fluent.ClockAlarm, contentDescription = null) },
        onClick = {
          dateTimePicker.showDatePicker(LocalDate.now(), selectDateTitle) { date ->
            dateTimePicker.showTimePicker(LocalTime.now(), selectTimeTitle) { time ->
              onPicked(date.atTime(time))
            }
          }
        },
      )
    }
  }
}

@Composable
private fun TargetRow(
  modifier: Modifier = Modifier,
  text: String,
  icon: @Composable () -> Unit,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = modifier.clickable(onClick = onClick),
    headlineContent = { Text(text) },
    leadingContent = icon,
  )
}
