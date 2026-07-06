package com.elementary.tasks.settings.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.ui.common.fragment.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class DisablePinFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<DisablePinViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    DisablePinScreen(
      pin = state.pin,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
    )
  }

  private fun handleEvent(event: DisablePinEvent) {
    when (event) {
      DisablePinEvent.ShowPinMismatch -> toast(R.string.pin_not_match)
      DisablePinEvent.PinCleared -> moveBack()
    }
  }

  override fun getTitle(): String = getString(R.string.disable_pin)
}
