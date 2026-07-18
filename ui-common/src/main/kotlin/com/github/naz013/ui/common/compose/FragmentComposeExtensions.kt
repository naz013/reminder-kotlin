package com.github.naz013.ui.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.github.naz013.ui.common.theme.ThemeModeHolder
import org.koin.android.ext.android.inject

fun Fragment.composeView(content: @Composable () -> Unit): ComposeView {
  val themeModeHolder: ThemeModeHolder by inject()
  return ComposeView(requireContext()).apply {
    setContent { AppTheme(darkTheme = themeModeHolder.resolveDarkTheme()) { content() } }
  }
}
