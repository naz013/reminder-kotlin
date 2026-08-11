package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.preview.AppScreenSizePreviews

/**
 * Wraps Material3's `NavigationSuiteScaffold`: a bottom navigation bar on compact width, a
 * navigation rail on medium/expanded width, chosen automatically from the current window size
 * class. Selection state and the resulting navigation action are entirely the caller's
 * responsibility - this only renders the chrome around [content].
 */
@Composable
fun <T> AppNavigationScaffold(
  destinations: List<AppDestination<T>>,
  selectedKey: T,
  onDestinationSelected: (T) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  NavigationSuiteScaffold(
    modifier = modifier,
    navigationSuiteItems = {
      destinations.forEach { destination ->
        val selected = destination.key == selectedKey
        val showBadge = (destination.badgeCount ?: 0) > 0
        item(
          selected = selected,
          onClick = { onDestinationSelected(destination.key) },
          icon = {
            Icon(
              painter = if (selected) destination.selectedIcon else destination.icon,
              contentDescription = stringResource(destination.labelRes),
            )
          },
          label = { Text(stringResource(destination.labelRes)) },
          badge = if (showBadge) ({ Badge { Text("${destination.badgeCount}") } }) else null,
        )
      }
    },
    content = content,
  )
}

@AppScreenSizePreviews
@Composable
private fun AppNavigationScaffoldPreview() {
  val destinations =
    listOf(
      AppDestination(key = "home", icon = AppIcons.Fluent.Calendar, labelRes = R.string.calendar),
      AppDestination(key = "notes", icon = AppIcons.Fluent.Text, labelRes = R.string.notes),
      AppDestination(
        key = "settings",
        icon = AppIcons.Fluent.Settings,
        labelRes = R.string.action_settings,
        badgeCount = 3,
      ),
    )
  var selectedKey by remember { mutableStateOf(destinations.first().key) }

  AppTheme {
    AppNavigationScaffold(
      destinations = destinations,
      selectedKey = selectedKey,
      onDestinationSelected = { selectedKey = it },
    ) {
      Box(modifier = Modifier.fillMaxSize())
    }
  }
}
