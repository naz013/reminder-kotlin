package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.SplitButton

/**
 * Renders [mainAction] as a full-width primary button. When [secondaryActions] is
 * non-empty, renders a split button instead, with the extra actions in a popup menu
 * behind the right-hand side of the button.
 */
@Composable
fun <T> ActionButtonsSection(
  modifier: Modifier = Modifier,
  mainAction: ScreenActionItem<T>,
  secondaryActions: List<ScreenActionItem<T>>,
  onActionClick: (T) -> Unit,
) {
  if (secondaryActions.isEmpty()) {
    Button(
      onClick = { onActionClick(mainAction.action) },
      modifier = modifier
        .fillMaxWidth()
        .height(56.dp),
      shape = ButtonDefaults.shape,
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
      ),
    ) {
      Icon(
        painter = painterResource(id = mainAction.iconRes),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = mainAction.text,
        style = MaterialTheme.typography.titleMediumEmphasized,
      )
    }
  } else {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
      SplitButton(
        onLeftClick = { onActionClick(mainAction.action) },
        onRightClick = { expanded = true },
        leftContent = {
          Icon(
            painter = painterResource(id = mainAction.iconRes),
            contentDescription = mainAction.text,
          )
          Text(
            text = mainAction.text,
            modifier = Modifier.padding(start = 8.dp),
          )
        },
        rightContent = {
          Box {
            Icon(
              painter = AppIcons.Fluent.MoreHor,
              contentDescription = stringResource(R.string.more_options),
            )
            PopupMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
              items = secondaryActions.mapIndexed { index, item ->
                PopupMenuItem(
                  id = index,
                  title = item.text,
                  iconRes = item.iconRes,
                )
              },
              onItemClick = { itemId ->
                secondaryActions.getOrNull(itemId)?.let { onActionClick(it.action) }
              },
            )
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        cornerRadius = 28.dp,
      )
    }
  }
}
