package com.github.naz013.feature.places.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

@Composable
fun PlaceListItemCard(
  place: PlaceState,
  onClick: () -> Unit,
  onMenuAction: (PlaceMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_fluent_place),
        contentDescription = null,
        tint = place.markerColor,
        modifier =
          Modifier
            .padding(start = 8.dp)
            .size(20.dp),
      )
      Text(
        text = place.name,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
          Modifier
            .weight(1f)
            .padding(start = 12.dp),
      )
      Box {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_more_vertical),
          contentDescription = stringResource(R.string.more_options),
          onClick = { menuExpanded = true },
        )
        AppDropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          items = placeMenuItems(),
          onItemClick = { id ->
            menuExpanded = false
            onMenuAction(PlaceMenuAction.entries[id])
          },
        )
      }
    }
  }
}

@Composable
private fun placeMenuItems(): List<PopupMenuItem> =
  listOf(
    PlaceMenuAction.EDIT to (R.string.edit to R.drawable.ic_fluent_edit),
    PlaceMenuAction.SHARE to (R.string.share to R.drawable.ic_fluent_share),
    PlaceMenuAction.DELETE to (R.string.delete to R.drawable.ic_fluent_delete),
  ).map { (action, titleAndIcon) ->
    val (titleRes, iconRes) = titleAndIcon
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = iconRes)
  }
