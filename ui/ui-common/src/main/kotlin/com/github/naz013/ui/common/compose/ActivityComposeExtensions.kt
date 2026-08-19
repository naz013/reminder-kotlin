package com.github.naz013.ui.common.compose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.ui.common.theme.ThemeModeHolder
import org.koin.android.ext.android.inject

/**
 * Resolves to the explicit Light/Dark override reactively, or - for "System default" - to
 * Compose's own [isSystemInDarkTheme], which recomposes on its own whenever the device's live
 * dark-mode setting changes. Neither path needs the hosting Activity to recreate.
 */
@Composable
internal fun ThemeModeHolder.resolveDarkTheme(): Boolean =
  when (nightMode) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
  }

fun ComponentActivity.composeView(content: @Composable () -> Unit) {
  val themeModeHolder: ThemeModeHolder by inject()
  val buildInfo: BuildInfo by inject()
  setContent {
    // Exposes every Modifier.testTag() as the accessibility node's resource-id (debug builds
    // only), which is what lets a black-box driver like Maestro (`id: <tag>` selector) locate an
    // element that has no text/contentDescription of its own - Compose UI tests already see
    // testTag via the semantics tree directly, without this. See docs/e2e-testing.md's note on
    // avoiding fixed-coordinate taps in Maestro flows for why this exists. `testTagsAsResourceId`
    // is a semantics property here (not a CompositionLocal - this Compose UI version has no
    // `LocalTestTagsAsResourceId`, confirmed by inspecting the resolved `ui-android` artifact's
    // actual classes), so it needs a real Modifier-bearing node to merge down from.
    Box(modifier = Modifier.semantics { testTagsAsResourceId = buildInfo.isDebug }) {
      AppTheme(darkTheme = themeModeHolder.resolveDarkTheme()) { content() }
    }
  }
}
