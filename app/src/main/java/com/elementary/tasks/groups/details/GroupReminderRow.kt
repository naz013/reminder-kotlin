package com.elementary.tasks.groups.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elementary.tasks.home.agenda.AgendaListItem
import com.elementary.tasks.reminder.lists.data.UiReminderList

/** Thin wrapper around [AgendaListItem] for a group's reminders: tap-through only, no per-row menu. */
@Composable
fun GroupReminderRow(
  item: UiReminderList,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AgendaListItem(
    mainText = item.mainText.text,
    secondaryText = item.secondaryText?.text,
    tertiaryText = item.tertiaryText?.text,
    tags = item.tags.map { it.text },
    onClick = onClick,
    menuItems = emptyList(),
    onMenuItemClick = {},
    modifier = modifier,
  )
}
