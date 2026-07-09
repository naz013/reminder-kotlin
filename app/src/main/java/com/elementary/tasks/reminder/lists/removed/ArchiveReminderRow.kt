package com.elementary.tasks.reminder.lists.removed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import com.elementary.tasks.home.eventsview.EventListItem
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

/**
 * Thin domain wrapper around [EventListItem] for archived reminders: Edit/Delete only, mirroring
 * [com.elementary.tasks.reminder.lists.ReminderActionResolver]'s `isRemoved` branching (the legacy
 * "Open" popup entry was wired to a no-op and is intentionally dropped here).
 */
@Composable
fun ArchiveReminderRow(
  item: UiReminderList,
  onClick: () -> Unit,
  onMenuAction: (ArchiveReminderMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  EventListItem(
    mainText = item.mainText.text,
    secondaryText = item.secondaryText?.text,
    tertiaryText = item.tertiaryText?.text,
    tags = item.tags.map { it.text },
    onClick = onClick,
    menuItems =
      listOf(
        PopupMenuItem(
          id = ArchiveReminderMenuAction.EDIT.ordinal,
          title = stringResource(R.string.edit),
          iconRes = R.drawable.ic_fluent_edit,
        ),
        PopupMenuItem(
          id = ArchiveReminderMenuAction.DELETE.ordinal,
          title = stringResource(R.string.delete),
          iconRes = R.drawable.ic_fluent_delete,
        ),
      ),
    onMenuItemClick = { id -> onMenuAction(ArchiveReminderMenuAction.entries[id]) },
    modifier = modifier,
  )
}
