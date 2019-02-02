package com.elementary.tasks.navigation.settings.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsDeletePinBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

class DisablePinFragment : BaseSettingsFragment<FragmentSettingsDeletePinBinding>() {

    private val mTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.pinLayout.isErrorEnabled = false
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_delete_pin

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveButton.setOnClickListener { savePin() }
        binding.pinField.addTextChangedListener(mTextWatcher)
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