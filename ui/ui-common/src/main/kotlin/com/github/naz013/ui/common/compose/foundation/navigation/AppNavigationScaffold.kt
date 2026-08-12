package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.isDesktopScreen
import com.github.naz013.ui.common.compose.foundation.isTabletScreen
import com.github.naz013.ui.common.compose.foundation.preview.AppScreenSizePreviews
import kotlinx.coroutines.launch

// Material3's collapsed WideNavigationRail width - not exposed as a public constant, so mirrored
// here to center the header's menu button against the (also centered, in collapsed state) item
// icons below it.
private val RailCollapsedWidth = 96.dp

// WideNavigationRail's header sits well below a standard 64dp TopAppBar's content row (measured:
// the menu button centers 36dp lower than a TopAppBar title/nav-icon does on the same screen).
// Shifting it up by that amount lines the toggle button up with the app bar in whatever screen is
// showing in the content pane, so the rail reads as part of the same top row instead of floating
// below it.
private val RailHeaderAlignmentOffset = (-36).dp

/**
 * Top-level app navigation chrome around [content]: a bottom [NavigationBar] on Compact width, a
 * [WideNavigationRail] on Medium/Expanded width - following the
 * [Material 3 navigation rail guidelines](https://m3.material.io/components/navigation-rail/guidelines),
 * the rail starts collapsed (icon only) and can be expanded (icon + label) via the menu button in
 * its header, independent of window size. Selection state and the resulting navigation action are
 * entirely the caller's responsibility - this only renders the chrome.
 *
 * [selectedKey] is nullable: some callers (e.g. a quick-launch panel where every destination
 * navigates away rather than swapping a persistent tab) never have a "current" destination -
 * pass `null` and every item renders unselected.
 *
 * [railState] defaults to a freshly `remember`ed state, which is fine for a caller composed once
 * for the app's lifetime. A caller re-composed from scratch on navigation (e.g. content wrapped
 * per-destination by a Nav3 `SceneDecoratorStrategy`) must instead hoist a [WideNavigationRailState]
 * above that per-destination composition and pass it in here, or the expanded/collapsed choice
 * resets on every navigation.
 */
@Composable
fun <T> AppNavigationScaffold(
  destinations: List<AppDestination<T>>,
  selectedKey: T?,
  onDestinationSelected: (T) -> Unit,
  modifier: Modifier = Modifier,
  railState: WideNavigationRailState = rememberWideNavigationRailState(),
  content: @Composable () -> Unit,
) {
  if (isTabletScreen() || isDesktopScreen()) {
    val coroutineScope = rememberCoroutineScope()
    val railExpanded = railState.currentValue == WideNavigationRailValue.Expanded

    Row(modifier = modifier.fillMaxSize()) {
      val toggleDescriptionRes = if (railExpanded) R.string.collapse_navigation else R.string.expand_navigation
      WideNavigationRail(
        state = railState,
        header = {
          // Fixed (not fillMaxWidth) so this centers correctly even when WideNavigationRail
          // measures the header under unbounded width constraints - matches the collapsed rail's
          // own width so the button lines up with the centered item icons below it.
          Box(
            modifier = Modifier.width(RailCollapsedWidth).offset(y = RailHeaderAlignmentOffset),
            contentAlignment = Alignment.Center,
          ) {
            MenuIconButton(
              icon = Icons.Default.Menu,
              contentDescription = stringResource(toggleDescriptionRes),
              onClick = { coroutineScope.launch { railState.toggle() } },
            )
          }
        },
      ) {
        destinations.forEach { destination ->
          val selected = destination.key == selectedKey
          WideNavigationRailItem(
            selected = selected,
            onClick = { onDestinationSelected(destination.key) },
            icon = {
              AppDestinationIcon(
                destination = destination,
                selected = selected,
              )
            },
            label = { Text(stringResource(destination.labelRes)) },
            railExpanded = railExpanded,
          )
        }
      }
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        content()
      }
    }
  } else {
    Column(modifier = modifier.fillMaxSize()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        content()
      }
      NavigationBar {
        destinations.forEach { destination ->
          val selected = destination.key == selectedKey
          NavigationBarItem(
            selected = selected,
            onClick = { onDestinationSelected(destination.key) },
            icon = {
              AppDestinationIcon(
                destination = destination,
                selected = selected,
              )
            },
            label = { Text(stringResource(destination.labelRes)) },
          )
        }
      }
    }
  }
}

@Composable
private fun <T> AppDestinationIcon(destination: AppDestination<T>, selected: Boolean) {
  val icon = if (selected) destination.selectedIcon else destination.icon
  val contentDescription = stringResource(destination.labelRes)
  val badgeCount = destination.badgeCount ?: 0
  if (badgeCount > 0) {
    BadgedBox(badge = { Badge { Text("$badgeCount") } }) {
      Icon(painter = icon, contentDescription = contentDescription)
    }
  } else {
    Icon(painter = icon, contentDescription = contentDescription)
  }
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

@AppScreenSizePreviews
@Composable
private fun AppNavigationScaffoldNoSelectionPreview() {
  val destinations =
    listOf(
      AppDestination(key = "calendar", icon = AppIcons.Fluent.Calendar, labelRes = R.string.calendar),
      AppDestination(key = "notes", icon = AppIcons.Fluent.Text, labelRes = R.string.notes),
    )

  AppTheme {
    AppNavigationScaffold(
      destinations = destinations,
      selectedKey = null,
      onDestinationSelected = {},
    ) {
      Box(modifier = Modifier.fillMaxSize())
    }
  }
}
