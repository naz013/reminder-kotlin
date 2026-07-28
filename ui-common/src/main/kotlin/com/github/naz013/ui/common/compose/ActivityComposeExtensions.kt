package com.github.naz013.ui.common.compose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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
  setContent { AppTheme(darkTheme = themeModeHolder.resolveDarkTheme()) { content() } }
}
