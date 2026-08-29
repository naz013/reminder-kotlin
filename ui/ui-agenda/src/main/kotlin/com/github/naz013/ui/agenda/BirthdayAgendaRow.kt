package com.github.naz013.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.AgendaListItem
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.toColor

private val COLOR_DOT_SIZE = 12.dp

/**
 * Thin domain wrapper around [AgendaListItem] for birthdays: a colored dot as the leading slot and
 * a menu mirroring `BirthdayResolver`'s Open/Edit/Delete actions. Pass `onMenuAction = null`
 * (e.g. in a read-only browsing context) to render the row without the menu at all.
 */
@Composable
fun BirthdayAgendaRow(
  item: UiAgendaBirthday,
  onClick: () -> Unit,
  onMenuAction: ((AgendaMenuAction) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  AgendaListItem(
    mainText = item.name,
    secondaryText = item.dateFormatted,
    tertiaryText = item.remainingTimeFormatted,
    tags = listOfNotNull(item.ageFormatted.takeIf { it.isNotEmpty() }),
    onClick = onClick,
    menuItems = if (onMenuAction != null) birthdayMenuItems() else emptyList(),
    onMenuItemClick = { id -> onMenuAction?.invoke(AgendaMenuAction.entries[id]) },
    modifier = modifier,
    leading = { ColorDot(color = item.color) },
  )
}

@Composable
private fun ColorDot(
  color: Int,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(COLOR_DOT_SIZE)
        .clip(CircleShape)
        .background(color.toColor()),
  )
}

@Composable
private fun birthdayMenuItems(): List<PopupMenuItem> =
  listOf(
    AgendaMenuAction.OPEN to R.string.open,
    AgendaMenuAction.EDIT to R.string.edit,
    AgendaMenuAction.DELETE to R.string.delete,
  ).map { (action, titleRes) ->
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = action.iconResOrNull())
  }

private fun AgendaMenuAction.iconResOrNull(): Int? =
  when (this) {
    AgendaMenuAction.OPEN -> R.drawable.ic_fluent_open
    AgendaMenuAction.EDIT -> R.drawable.ic_fluent_edit
    AgendaMenuAction.DELETE -> R.drawable.ic_fluent_delete
    AgendaMenuAction.ARCHIVE, AgendaMenuAction.SKIP, AgendaMenuAction.TURN_OFF,
    AgendaMenuAction.PIN, AgendaMenuAction.UNPIN,
    -> null
  }
