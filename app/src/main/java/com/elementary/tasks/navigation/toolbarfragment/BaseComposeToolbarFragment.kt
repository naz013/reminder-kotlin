package com.elementary.tasks.navigation.toolbarfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import com.github.naz013.ui.common.compose.composeView

abstract class BaseComposeToolbarFragment : ToolbarFragment() {
  @Composable
  protected abstract fun Content()

  final override fun onCreateContentView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle?,
  ): View = composeView { Content() }
}
