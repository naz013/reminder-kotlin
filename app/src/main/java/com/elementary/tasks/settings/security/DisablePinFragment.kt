package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.feature.common.android.toast
import com.elementary.tasks.databinding.FragmentSettingsDeletePinBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment

class DisablePinFragment : BaseSettingsFragment<FragmentSettingsDeletePinBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsDeletePinBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.pinView.supportFinger = false
    binding.pinView.callback = { onPinChanged(it) }
  }

  private fun onPinChanged(pin: String) {
    if (pin.length < 6) return
    if (prefs.pinCode == pin) {
      prefs.pinCode = ""
      moveBack()
    } else {
      toast(R.string.pin_not_match)
      binding.pinView.clearPin()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    callback?.hideKeyboard()
  }

  override fun getTitle(): String = getString(R.string.disable_pin)
}
