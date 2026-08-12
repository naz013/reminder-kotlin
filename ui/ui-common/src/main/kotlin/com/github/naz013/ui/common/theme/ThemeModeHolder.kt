package com.github.naz013.ui.common.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Reactive counterpart to [ThemePreferences.nightMode]: a Compose-observable snapshot of the
 * user's day/night choice, kept in sync by the Settings screen whenever it writes a new value to
 * [ThemePreferences]. Reading it inside a composable (see `composeView` in
 * `ActivityComposeExtensions.kt`) means a theme change recomposes immediately, with no
 * `Activity.recreate()` needed.
 */
class ThemeModeHolder(
  themePreferences: ThemePreferences,
) {
  var nightMode by mutableIntStateOf(themePreferences.nightMode)
}
