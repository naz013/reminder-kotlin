package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.feature.common.android.toast
import com.elementary.tasks.databinding.FragmentSettingsChangePinBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment

class ChangePinFragment : BaseSettingsFragment<FragmentSettingsChangePinBinding>() {

  private var state: State = State.OLD
  private var pin: String = ""

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsChangePinBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.pinView.supportFinger = false
    binding.pinView.callback = { onPinChanged(it) }

    onStateChanged(State.OLD)
  }

  private fun onPinChanged(pin: String) {
    if (pin.length < 6) return
    when (state) {
      State.OLD -> {
        if (prefs.pinCode == pin) {
          onStateChanged(State.INPUT)
        } else {
          toast(R.string.pin_not_match)
          binding.pinView.clearPin()
        }
      }
      State.INPUT -> {
        this.pin = pin
        onStateChanged(State.REPEAT)
      }
      State.REPEAT -> {
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
  }

  private fun onStateChanged(state: State) {
    binding.pinView.clearPin()
    this.state = state
    when (state) {
      State.OLD -> {
        binding.messageView.text = getString(R.string.old_pin)
      }
      State.INPUT -> {
        binding.messageView.text = getString(R.string.enter_pin)
      }
      State.REPEAT -> {
        binding.messageView.text = getString(R.string.repeat_pin)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    callback?.hideKeyboard()
  }

  override fun getTitle(): String = getString(R.string.change_pin)

  private enum class State {
    OLD, INPUT, REPEAT
  }
}
