package com.elementary.tasks.home.eventsview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.toColor

private val COLOR_DOT_SIZE = 12.dp

/**
 * Thin domain wrapper around [EventListItem] for birthdays: a colored dot as the leading slot and
 * a menu mirroring `BirthdayResolver`'s Open/Edit/Delete actions.
 */
@Composable
fun BirthdayEventRow(
  item: UiEventBirthday,
  onClick: () -> Unit,
  onMenuAction: (EventMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  EventListItem(
    mainText = item.name,
    secondaryText = item.dateFormatted,
    tertiaryText = item.remainingTimeFormatted,
    tags = listOfNotNull(item.ageFormatted.takeIf { it.isNotEmpty() }),
    onClick = onClick,
    menuItems = birthdayMenuItems(),
    onMenuItemClick = { id -> onMenuAction(EventMenuAction.entries[id]) },
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
    EventMenuAction.OPEN to R.string.open,
    EventMenuAction.EDIT to R.string.edit,
    EventMenuAction.DELETE to R.string.delete,
  ).map { (action, titleRes) ->
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = action.iconResOrNull())
  }

private fun EventMenuAction.iconResOrNull(): Int? =
  when (this) {
    EventMenuAction.OPEN -> R.drawable.ic_fluent_open
    EventMenuAction.EDIT -> R.drawable.ic_fluent_edit
    EventMenuAction.DELETE -> R.drawable.ic_fluent_delete
    EventMenuAction.ARCHIVE, EventMenuAction.SKIP, EventMenuAction.TURN_OFF -> null
  }
