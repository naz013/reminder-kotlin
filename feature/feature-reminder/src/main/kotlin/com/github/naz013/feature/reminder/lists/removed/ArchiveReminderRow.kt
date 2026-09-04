package com.github.naz013.feature.reminder.lists.removed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.AgendaListItem
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

@Composable
fun ArchiveReminderRow(
  item: UiReminderList,
  onClick: () -> Unit,
  onMenuAction: (ArchiveReminderMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  AgendaListItem(
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
    isHighlighted = item.isSelected,
  )
}
