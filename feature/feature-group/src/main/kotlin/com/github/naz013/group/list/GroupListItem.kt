package com.github.naz013.group.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.group.UiGroupList

private val COLOR_DOT_SIZE = 14.dp

@Composable
internal fun GroupListItem(
  group: UiGroupList,
  onClick: () -> Unit,
  onMenuAction: (GroupMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(
      containerColor = if (group.isSelected) MaterialTheme.colorScheme.primaryContainer else CardDefaults.cardColors().containerColor,
    ),
    border = if (group.isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(COLOR_DOT_SIZE)
            .clip(CircleShape)
            .background(group.color.toColor()),
      )
      Column(
        modifier = Modifier.weight(1f).padding(start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        if (group.isDefaultGroup) {
          DefaultChip()
        }
        Text(
          text = group.title,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth(),
        )
        Text(
          text = group.reminderCountText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Box {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_more_vertical),
          contentDescription = stringResource(R.string.more_options),
          onClick = { menuExpanded = true },
        )
        AppDropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          items = groupMenuItems(group.canDelete, group.canSetAsDefault),
          onItemClick = { id ->
            menuExpanded = false
            onMenuAction(GroupMenuAction.entries[id])
          },
        )
      }
    }
  }
}

@Composable
private fun DefaultChip() {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.tertiaryContainer,
  ) {
    Text(
      text = stringResource(R.string.default_string),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onTertiaryContainer,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
    )
  }
}

@Composable
private fun groupMenuItems(
  canDelete: Boolean,
  canSetAsDefault: Boolean,
): List<PopupMenuItem> {
  val actions =
    listOfNotNull(
      if (canSetAsDefault) {
        GroupMenuAction.MAKE_DEFAULT to R.string.make_default
      } else {
        null
      },
      GroupMenuAction.EDIT to R.string.edit,
      if (canDelete) {
        GroupMenuAction.DELETE to R.string.delete
      } else {
        null
      },
    )
  return actions.map { (action, titleRes) ->
    PopupMenuItem(
      id = action.ordinal,
      title = stringResource(titleRes),
      iconRes = action.iconResOrNull(),
    )
  }
}

private fun GroupMenuAction.iconResOrNull(): Int? =
  when (this) {
    GroupMenuAction.EDIT -> R.drawable.ic_fluent_edit
    GroupMenuAction.DELETE -> R.drawable.ic_fluent_delete
    GroupMenuAction.MAKE_DEFAULT -> R.drawable.ic_fluent_star
  }
