package com.elementary.tasks.settings.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapStyleFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<MapStyleViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()

    MapStyleScreen(
      state = state,
      onOptionSelected = viewModel::onOptionSelected,
    )
  }

  override fun getTitle(): String = getString(R.string.map_style)
}
