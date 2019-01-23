package com.elementary.tasks.navigation.settings.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_change_pin.*

class ChangePinFragment : BaseSettingsFragment() {

    private val mTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            pinLayout.isErrorEnabled = false
            pinConfirmLayout.isErrorEnabled = false
            pinOldLayout.isErrorEnabled = false
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_change_pin

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveButton.setOnClickListener { savePin() }

        pinField.addTextChangedListener(mTextWatcher)
        pinConfirmField.addTextChangedListener(mTextWatcher)
        pinOldField.addTextChangedListener(mTextWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        callback?.hideKeyboard()
    }

    private fun savePin() {
        val old = pinOldField.text.toString().trim()
        val new = pinField.text.toString().trim()
        val confirm = pinConfirmField.text.toString().trim()

        var hasError = false
        if (old.length < 6) {
            pinOldLayout.error = getString(R.string.wrong_pin)
            pinOldLayout.isErrorEnabled = true
            hasError = true
        }
        if (new.length < 6) {
            pinLayout.error = getString(R.string.wrong_pin)
            pinLayout.isErrorEnabled = true
            hasError = true
        }
        if (confirm.length < 6) {
            pinConfirmLayout.error = getString(R.string.wrong_pin)
            pinConfirmLayout.isErrorEnabled = true
            hasError = true
        }

        if (!hasError) {
            if (old != prefs.pinCode) {
                hasError = true
                pinOldLayout.error = getString(R.string.pin_not_match)
                pinOldLayout.isErrorEnabled = true
            } else if (new != confirm) {
                hasError = true
                pinLayout.error = getString(R.string.pin_not_match)
                pinLayout.isErrorEnabled = true
                pinConfirmLayout.error = getString(R.string.pin_not_match)
                pinConfirmLayout.isErrorEnabled = true
            }
        }

        if (hasError) return

        prefs.pinCode = new
        moveBack()
    }

    override fun getTitle(): String = getString(R.string.change_pin)
}