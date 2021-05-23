package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.onTextChanged
import com.elementary.tasks.databinding.FragmentSettingsDeletePinBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class DisablePinFragment : BaseSettingsFragment<FragmentSettingsDeletePinBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsDeletePinBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.saveButton.setOnClickListener { savePin() }
    binding.pinField.onTextChanged {
      binding.pinLayout.isErrorEnabled = false
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    callback?.hideKeyboard()
  }

  private fun savePin() {
    val old = binding.pinField.text.toString().trim()

    var hasError = false
    if (old.length < 6) {
      binding.pinLayout.error = getString(R.string.wrong_pin)
      binding.pinLayout.isErrorEnabled = true
      hasError = true
    }
    if (!hasError) {
      if (old != prefs.pinCode) {
        hasError = true
        binding.pinLayout.error = getString(R.string.pin_not_match)
        binding.pinLayout.isErrorEnabled = true
      }
    }

    if (hasError) return

    prefs.pinCode = ""
    moveBack()
  }

  override fun getTitle(): String = getString(R.string.disable_pin)
}