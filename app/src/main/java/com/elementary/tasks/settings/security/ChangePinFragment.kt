package com.elementary.tasks.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.onTextChanged
import com.elementary.tasks.databinding.FragmentSettingsChangePinBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class ChangePinFragment : BaseSettingsFragment<FragmentSettingsChangePinBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsChangePinBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.saveButton.setOnClickListener { savePin() }

    binding.pinField.onTextChanged { hideErrors() }
    binding.pinConfirmField.onTextChanged { hideErrors() }
    binding.pinOldField.onTextChanged { hideErrors() }
  }

  private fun hideErrors() {
    binding.pinLayout.isErrorEnabled = false
    binding.pinConfirmLayout.isErrorEnabled = false
    binding.pinOldLayout.isErrorEnabled = false
  }

  override fun onDestroy() {
    super.onDestroy()
    callback?.hideKeyboard()
  }

  private fun savePin() {
    val old = binding.pinOldField.text.toString().trim()
    val new = binding.pinField.text.toString().trim()
    val confirm = binding.pinConfirmField.text.toString().trim()

    var hasError = false
    if (old.length < 6) {
      binding.pinOldLayout.error = getString(R.string.wrong_pin)
      binding.pinOldLayout.isErrorEnabled = true
      hasError = true
    }
    if (new.length < 6) {
      binding.pinLayout.error = getString(R.string.wrong_pin)
      binding.pinLayout.isErrorEnabled = true
      hasError = true
    }
    if (confirm.length < 6) {
      binding.pinConfirmLayout.error = getString(R.string.wrong_pin)
      binding.pinConfirmLayout.isErrorEnabled = true
      hasError = true
    }

    if (!hasError) {
      if (old != prefs.pinCode) {
        hasError = true
        binding.pinOldLayout.error = getString(R.string.pin_not_match)
        binding.pinOldLayout.isErrorEnabled = true
      } else if (new != confirm) {
        hasError = true
        binding.pinLayout.error = getString(R.string.pin_not_match)
        binding.pinLayout.isErrorEnabled = true
        binding.pinConfirmLayout.error = getString(R.string.pin_not_match)
        binding.pinConfirmLayout.isErrorEnabled = true
      }
    }

    if (hasError) return

    prefs.pinCode = new
    moveBack()
  }

  override fun getTitle(): String = getString(R.string.change_pin)
}