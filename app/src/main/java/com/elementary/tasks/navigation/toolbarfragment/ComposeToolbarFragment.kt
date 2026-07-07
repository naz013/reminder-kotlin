package com.elementary.tasks.navigation.toolbarfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import com.github.naz013.ui.common.compose.composeView

/**
 * [ToolbarFragment] variant whose content is Jetpack Compose instead of a [androidx.viewbinding.ViewBinding].
 * Keeps the shared collapsing toolbar/back-navigation chrome; subclasses only provide
 * [FragmentContent].
 */
abstract class ComposeToolbarFragment : ToolbarFragment() {
  final override fun onCreateContentView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle?,
  ): View = composeView { FragmentContent() }

  @Composable
  abstract fun FragmentContent()
}
