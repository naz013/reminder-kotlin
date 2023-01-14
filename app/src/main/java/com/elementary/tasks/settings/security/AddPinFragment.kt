package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.databinding.FragmentSettingsAddPinBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class AddPinFragment : BaseSettingsFragment<FragmentSettingsAddPinBinding>() {

  private var state: State = State.INPUT
  private var pin: String = ""

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsAddPinBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.pinView.supportFinger = false
    binding.pinView.callback = { onPinChanged(it) }

    onStateChanged(State.INPUT)
  }

  private fun onPinChanged(pin: String) {
    if (pin.length < 6) return
    if (state == State.INPUT) {
      this.pin = pin
      onStateChanged(State.REPEAT)
    } else {
      if (this.pin == pin) {
        prefs.pinCode = pin
        moveBack()
      } else {
        toast(R.string.pin_not_match)
        onStateChanged(State.INPUT)
      }
      this.pin = ""
    }
  }

  private fun onStateChanged(state: State) {
    binding.pinView.clearPin()
    this.state = state
    if (state == State.INPUT) {
      binding.messageView.text = getString(R.string.enter_pin)
    } else {
      binding.messageView.text = getString(R.string.repeat_pin)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    callback?.hideKeyboard()
  }

  override fun getTitle(): String = getString(R.string.add_pin)

  private enum class State {
    INPUT, REPEAT
  }
}
