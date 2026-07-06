package com.elementary.tasks.settings.reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.reminder.build.preset.ManagePresetsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManagePresetsFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<ManagePresetsViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()

    ManagePresetsScreen(
      presets = state.presets,
      onDeleteClick = { viewModel.deletePreset(it.id) },
    )
  }

  override fun getTitle(): String = getString(R.string.recur_presets)
}
