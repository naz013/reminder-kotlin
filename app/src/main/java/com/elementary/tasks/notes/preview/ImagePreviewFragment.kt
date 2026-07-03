package com.elementary.tasks.notes.preview

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.compose.ComposeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ImagePreviewFragment : ComposeFragment() {
  private val viewModel by viewModel<ImagePreviewViewModel> { parametersOf(positionFromArgs()) }

  private fun positionFromArgs(): Int = arguments?.getInt(IntentKeys.INTENT_POSITION, 0) ?: 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.saveStatusBarColor(activity?.window?.statusBarColor ?: -1)
  }

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsState()
    val colors = viewModel.colorsFor(state)

    SideEffect {
      colors.statusBarColor?.let {
        activity?.window?.statusBarColor = it
        activity?.window?.navigationBarColor = it
      }
    }

    ImagePreviewScreen(
      state = state,
      colors = colors,
      onBackClick = { moveBack() },
      onPageChanged = { viewModel.onPageChanged(it) },
    )
  }

  override fun onPause() {
    super.onPause()
    viewModel.getStatusBarColor()?.also {
      activity?.window?.statusBarColor = it
      activity?.window?.navigationBarColor = it
    }
  }

  private fun moveBack() {
    activity?.onBackPressedDispatcher?.onBackPressed()
  }
}
