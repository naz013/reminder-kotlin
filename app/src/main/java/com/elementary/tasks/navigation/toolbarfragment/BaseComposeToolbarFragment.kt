package com.elementary.tasks.navigation.toolbarfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import com.github.naz013.ui.common.compose.composeView

/**
 * Compose counterpart of [BaseToolbarFragment]. Provides the same AppBar/Toolbar scaffold
 * (title, navigation icon, options menu via [FragmentMenuController]) without requiring a
 * [androidx.viewbinding.ViewBinding] - content is a plain composable instead.
 */
abstract class BaseComposeToolbarFragment : ToolbarFragment() {
  @Composable
  protected abstract fun Content()

  final override fun onCreateContentView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle?,
  ): View = composeView { Content() }
}
