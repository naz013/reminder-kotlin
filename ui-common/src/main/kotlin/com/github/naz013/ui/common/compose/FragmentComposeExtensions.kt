package com.github.naz013.ui.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

fun Fragment.composeView(content: @Composable () -> Unit): ComposeView {
  return ComposeView(requireContext()).apply {
    setContent { AppTheme { content() } }
  }
}
