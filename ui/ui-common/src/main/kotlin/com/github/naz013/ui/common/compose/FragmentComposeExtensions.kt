package com.github.naz013.ui.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.fragment.app.Fragment
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.ui.common.theme.ThemeModeHolder
import org.koin.android.ext.android.inject

fun Fragment.composeView(content: @Composable () -> Unit): ComposeView {
  val themeModeHolder: ThemeModeHolder by inject()
  val buildInfo: BuildInfo by inject()
  return ComposeView(requireContext()).apply {
    setContent {
      // See ActivityComposeExtensions.kt's composeView() for why this is here.
      Box(modifier = Modifier.semantics { testTagsAsResourceId = buildInfo.isDebug }) {
        AppTheme(darkTheme = themeModeHolder.resolveDarkTheme()) { content() }
      }
    }
  }
}
