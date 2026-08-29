package com.github.naz013.tags.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.BirthdayAgendaRow
import com.github.naz013.ui.agenda.ReminderAgendaRow
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.birthday.UiBirthdayList
import com.github.naz013.ui.googletask.GoogleTaskRow
import com.github.naz013.ui.note.NoteCard
import com.github.naz013.ui.reminder.UiReminderList
import org.threeten.bp.LocalDateTime

@Composable
internal fun TagDetailItemRow(
  item: TagDetailItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (item) {
    is TagDetailItem.ReminderItem ->
      ReminderAgendaRow(
        item = item.ui.toAgendaReminder(),
        onClick = onClick,
        onMenuAction = null,
        modifier = modifier,
      )

    is TagDetailItem.NoteItem ->
      NoteCard(
        note = item.ui,
        onClick = onClick,
        modifier = modifier,
      )

    is TagDetailItem.BirthdayItem ->
      BirthdayAgendaRow(
        item = item.ui.toAgendaBirthday(),
        onClick = onClick,
        onMenuAction = null,
        modifier = modifier,
      )

    is TagDetailItem.GoogleTaskItem ->
      GoogleTaskRow(
        task = item.ui,
        onClick = onClick,
        onToggle = null,
        modifier = modifier,
      )
  }
}

private fun UiReminderList.toAgendaReminder() =
  UiAgendaReminder(
    id = id,
    dateTime = dueDateTime ?: LocalDateTime.MIN,
    category = AgendaCategory.REMINDERS,
    mainText = mainText,
    secondaryText = secondaryText,
    tertiaryText = tertiaryText,
    tags = tags,
    actions = actions,
    state = state,
  )

private fun UiBirthdayList.toAgendaBirthday() =
  UiAgendaBirthday(
    id = uuId,
    dateTime = nextBirthdayDate,
    name = name,
    ageFormatted = ageFormatted,
    remainingTimeFormatted = remainingTimeFormatted,
    color = color,
    contrastColor = contrastColor,
    dateFormatted = birthdayDateFormatted,
  )
