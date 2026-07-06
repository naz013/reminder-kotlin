package com.elementary.tasks.settings.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.ui.common.fragment.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPinFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<AddPinViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    AddPinScreen(
      stage = state.stage,
      pin = state.pin,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
    )
  }

  private fun handleEvent(event: AddPinEvent) {
    when (event) {
      AddPinEvent.ShowPinMismatch -> toast(R.string.pin_not_match)
      AddPinEvent.PinSaved -> moveBack()
    }
  }

  override fun getTitle(): String = getString(R.string.add_pin)
}
