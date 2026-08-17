package com.elementary.tasks.home.agenda

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.AgendaListItem
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

/**
 * Thin domain wrapper around [AgendaListItem] for reminder/shopping items: an "Enabled" status
 * chip above the title when active, and a menu mirroring `ReminderActionResolver`'s
 * Open/Edit/Move-to-archive/Skip (active) vs Open/Edit/Delete (removed) branching, plus a
 * "Turn off" action when active.
 */
@Composable
fun ReminderAgendaRow(
  item: UiAgendaReminder,
  onClick: () -> Unit,
  onMenuAction: (AgendaMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  AgendaListItem(
    mainText = item.mainText.text,
    secondaryText = item.secondaryText?.text,
    tertiaryText = item.tertiaryText?.text,
    tags = item.tags.map { it.text },
    statusChips = if (item.state.isActive) listOf(stringResource(R.string.enabled4)) else emptyList(),
    onClick = onClick,
    menuItems = reminderMenuItems(item),
    onMenuItemClick = { id -> onMenuAction(AgendaMenuAction.entries[id]) },
    modifier = modifier,
  )
}

@Composable
private fun reminderMenuItems(item: UiAgendaReminder): List<PopupMenuItem> {
  val actions =
    if (item.state.isRemoved) {
      listOf(
        AgendaMenuAction.EDIT to R.string.edit,
        AgendaMenuAction.DELETE to R.string.delete,
      )
    } else {
      buildList {
        if (item.state.isActive) {
          add(AgendaMenuAction.TURN_OFF to R.string.turn_off)
        }
        add(AgendaMenuAction.OPEN to R.string.open)
        add(AgendaMenuAction.EDIT to R.string.edit)
        add(AgendaMenuAction.ARCHIVE to R.string.move_to_archive)
        if (item.actions.canSkip) {
          add(AgendaMenuAction.SKIP to R.string.skip_event)
        }
      }
    }
  return actions.map { (action, titleRes) ->
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = action.iconResOrNull())
  }
}

private fun AgendaMenuAction.iconResOrNull(): Int? =
  when (this) {
    AgendaMenuAction.OPEN -> R.drawable.ic_fluent_open
    AgendaMenuAction.EDIT -> R.drawable.ic_fluent_edit
    AgendaMenuAction.ARCHIVE -> R.drawable.ic_fluent_archive
    AgendaMenuAction.DELETE -> R.drawable.ic_fluent_delete
    AgendaMenuAction.SKIP -> R.drawable.ic_fluent_approvals_app
    AgendaMenuAction.TURN_OFF -> R.drawable.ic_fluent_alert_off
  }
