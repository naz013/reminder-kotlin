package com.elementary.tasks.home.eventsview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

/**
 * Thin domain wrapper around [EventListItem] for reminder/shopping items: an "Enabled" status
 * chip above the title when active, and a menu mirroring `ReminderActionResolver`'s
 * Open/Edit/Move-to-archive/Skip (active) vs Open/Edit/Delete (removed) branching, plus a
 * "Turn off" action when active.
 */
@Composable
fun ReminderEventRow(
  item: UiEventReminder,
  onClick: () -> Unit,
  onMenuAction: (EventMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  EventListItem(
    mainText = item.mainText.text,
    secondaryText = item.secondaryText?.text,
    tertiaryText = item.tertiaryText?.text,
    tags = item.tags.map { it.text },
    statusChips = if (item.state.isActive) listOf(stringResource(R.string.enabled4)) else emptyList(),
    onClick = onClick,
    menuItems = reminderMenuItems(item),
    onMenuItemClick = { id -> onMenuAction(EventMenuAction.entries[id]) },
    modifier = modifier,
  )
}

@Composable
private fun reminderMenuItems(item: UiEventReminder): List<PopupMenuItem> {
  val actions =
    if (item.state.isRemoved) {
      listOf(
        EventMenuAction.EDIT to R.string.edit,
        EventMenuAction.DELETE to R.string.delete,
      )
    } else {
      buildList {
        if (item.state.isActive) {
          add(EventMenuAction.TURN_OFF to R.string.turn_off)
        }
        add(EventMenuAction.OPEN to R.string.open)
        add(EventMenuAction.EDIT to R.string.edit)
        add(EventMenuAction.ARCHIVE to R.string.move_to_archive)
        if (item.actions.canSkip) {
          add(EventMenuAction.SKIP to R.string.skip_event)
        }
      }
    }
  return actions.map { (action, titleRes) ->
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = action.iconResOrNull())
  }
}

private fun EventMenuAction.iconResOrNull(): Int? =
  when (this) {
    EventMenuAction.OPEN -> R.drawable.ic_fluent_open
    EventMenuAction.EDIT -> R.drawable.ic_fluent_edit
    EventMenuAction.ARCHIVE -> R.drawable.ic_fluent_archive
    EventMenuAction.DELETE -> R.drawable.ic_fluent_delete
    EventMenuAction.SKIP -> R.drawable.ic_fluent_approvals_app
    EventMenuAction.TURN_OFF -> R.drawable.ic_fluent_alert_off
  }
