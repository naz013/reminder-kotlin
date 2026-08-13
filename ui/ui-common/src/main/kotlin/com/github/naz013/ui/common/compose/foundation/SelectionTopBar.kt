package com.github.naz013.ui.common.compose.foundation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

/**
 * Contextual top bar for the app's multiselect pattern (see `docs/multiselect.md`): an X to
 * cancel the selection, a caller-supplied [title] (e.g. "N selected", "N notes selected", "N
 * groups selected" - whatever reads best for that screen), and every bulk operation that screen
 * supports (delete, archive, move, change color, ...) collapsed into a single three-dot menu -
 * so the bar looks and behaves the same on every screen regardless of how many actions it offers,
 * the same way the app's per-item overflow menus already do.
 *
 * Swap this in for the screen's normal top bar whenever `selectedCount > 0`. For the generic "N
 * selected" wording, build [title] with `stringResource(R.string.selected_count, selectedCount)`;
 * screens that want a noun in the title should define their own formatted string instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
  modifier: Modifier = Modifier,
  title: String,
  onCancelClick: () -> Unit,
  actions: List<PopupMenuItem>,
  onActionClick: (Int) -> Unit,
) {
  TopAppBar(
    modifier = modifier,
    title = { Text(title) },
    navigationIcon = {
      MenuIconButton(
        icon = AppIcons.Fluent.Dismiss,
        contentDescription = stringResource(R.string.cancel),
        onClick = onCancelClick,
      )
    },
    actions = {
      var menuExpanded by remember { mutableStateOf(false) }
      MenuIconButton(
        icon = painterResource(R.drawable.ic_fluent_more_vertical),
        contentDescription = stringResource(R.string.more_options),
        onClick = { menuExpanded = true },
      )
      AppDropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        items = actions,
        onItemClick = onActionClick,
      )
    },
    colors =
    TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
    ),
  )
}
