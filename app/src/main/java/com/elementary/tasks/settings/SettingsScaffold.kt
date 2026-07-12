package com.elementary.tasks.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

/**
 * Scaffold+TopAppBar replicating what
 * [com.elementary.tasks.navigation.toolbarfragment.ToolbarFragment] used to provide (title, back
 * icon, back click) for every Settings-tree screen - none of those screens' own composables owned
 * a Scaffold/TopAppBar themselves, unlike every other promoted screen (Notes, Groups, ...), since
 * that chrome used to live in the shared Fragment base instead. Shared here (rather than inlined
 * per screen like `GroupsScreen`/`BuildReminderScreen` do) because ~20 Settings screens need the
 * exact same title/back-icon/back-click behavior, including the `screenTitle`-driven "X to close"
 * icon swap a handful of them use (`RemindersSettingsFragment.getNavigationIcon()` and siblings).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
  title: String,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: Int = R.drawable.ic_builder_arrow_left,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          MenuIconButton(
            icon = painterResource(navigationIcon),
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    content = content,
  )
}

/** Back icon for a Settings screen reached with an explicit [screenTitle] override ("X to close"). */
fun settingsNavigationIcon(screenTitle: String?): Int =
  if (screenTitle == null) R.drawable.ic_builder_arrow_left else R.drawable.ic_builder_clear
