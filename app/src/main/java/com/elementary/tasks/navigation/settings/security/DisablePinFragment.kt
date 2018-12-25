package com.elementary.tasks.navigation.settings.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_delete_pin.*

class DisablePinFragment : BaseSettingsFragment() {

    private val mTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            pinLayout.isErrorEnabled = false
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_delete_pin

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveButton.setOnClickListener { savePin() }

        pinField.addTextChangedListener(mTextWatcher)
    }

    private fun savePin() {
        val old = pinField.text.toString().trim()

        var hasError = false
        if (old.length < 6) {
            pinLayout.error = getString(R.string.wrong_pin)
            pinLayout.isErrorEnabled = true
            hasError = true
        }
        if (!hasError) {
            if (old != prefs.pinCode) {
                hasError = true
                pinLayout.error = getString(R.string.pin_not_match)
                pinLayout.isErrorEnabled = true
            }
        }

        if (hasError) return

        prefs.pinCode = ""
        moveBack()
    }

    override fun getTitle(): String = getString(R.string.disable_pin)
}