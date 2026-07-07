package com.elementary.tasks.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.common.intent.IntentKeys
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoteSettingsFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<NoteSettingsViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    NoteSettingsScreen(
      state = state,
      onColorRememberToggle = viewModel::onColorRememberToggle,
      onFontSizeRememberToggle = viewModel::onFontSizeRememberToggle,
      onFontStyleRememberToggle = viewModel::onFontStyleRememberToggle,
      onOpacityClick = viewModel::onOpacityClick,
      onOpacityPreviewChange = viewModel::onOpacityPreviewChange,
      onOpacityConfirm = viewModel::onOpacityConfirm,
      onOpacityDialogDismiss = viewModel::onOpacityDialogDismiss,
    )
  }

  override fun getTitle(): String = arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.notes)

  override fun getNavigationIcon(): Int =
    if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
}
