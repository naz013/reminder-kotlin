package com.elementary.tasks.navigation.settings.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.FingerInitializer
import com.elementary.tasks.core.utils.FingerprintHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.navigation.MainActivity
import kotlinx.android.synthetic.main.activity_pin_login.*

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class PinLoginActivity : ThemedActivity(), FingerInitializer.ReadyListener, FingerprintHelper.Callback {

    private var fingerprintHelper: FingerprintHelper? = null
    private var isBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBack = intent.getBooleanExtra(ARG_BACK, false)
        setContentView(R.layout.activity_pin_login)

        if (Module.isPro) appNameBannerPro.visibility = View.VISIBLE
        else appNameBannerPro.visibility = View.GONE

        pinView.callback = {
            if (it.length == 6) {
                tryLogin(it)
            }
        }
        FingerInitializer(this, this, this)
    }

    private fun tryLogin(pin: String) {
        if (pin.length < 6) {
            Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
            pinView.clearPin()
            return
        }

        if (pin == prefs.pinCode) {
            if (isBack) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                openApplication()
            }
        } else {
            Toast.makeText(this, R.string.pin_not_match, Toast.LENGTH_SHORT).show()
            pinView.clearPin()
        }
    }

    private fun openApplication() {
        startActivity(Intent(this@PinLoginActivity, MainActivity::class.java))
        finish()
    }

    override fun onReady(context: Context, fingerprintUiHelper: FingerprintHelper) {
        this.fingerprintHelper = fingerprintUiHelper
        if (fingerprintUiHelper.canUseFinger(this)) {
            fingerIcon.visibility = View.VISIBLE
            fingerprintUiHelper.startListening(null)
        } else {
            fingerIcon.visibility = View.GONE
        }
    }

    override fun onFailToCreate() {
        fingerIcon.visibility = View.GONE
    }

    override fun onIdle() {
        fingerIcon.setImageResource(R.drawable.ic_twotone_fingerprint_secondary)
    }

    override fun onAuthenticated() {
        fingerIcon.setImageResource(R.drawable.ic_twotone_fingerprint_green)
        openApplication()
    }

    override fun onError() {
        fingerIcon.setImageResource(R.drawable.ic_twotone_fingerprint_red)
    }

    override fun onDestroy() {
        super.onDestroy()
        fingerprintHelper?.stopListening()
    }

    companion object {
        const val ARG_BACK = "arg_back"
    }
}
