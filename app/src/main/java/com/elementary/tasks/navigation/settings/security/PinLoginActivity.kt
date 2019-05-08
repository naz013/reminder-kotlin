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
import com.elementary.tasks.databinding.ActivityPinLoginBinding
import com.elementary.tasks.experimental.NavUtil

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
class PinLoginActivity : ThemedActivity<ActivityPinLoginBinding>(), FingerInitializer.ReadyListener, FingerprintHelper.Callback {

    private var fingerprintHelper: FingerprintHelper? = null
    private var isBack = false

    override fun layoutRes(): Int = R.layout.activity_pin_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBack = intent.getBooleanExtra(ARG_BACK, false)

        if (Module.isPro) binding.appNameBannerPro.visibility = View.VISIBLE
        else binding.appNameBannerPro.visibility = View.GONE

        binding.pinView.callback = {
            if (it.length == 6) {
                tryLogin(it)
            }
        }
        if (prefs.useFingerprint) {
            FingerInitializer(this, this, this)
        } else {
            binding.fingerIcon.visibility = View.GONE
        }
    }

    private fun tryLogin(pin: String) {
        if (pin.length < 6) {
            Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
            binding.pinView.clearPin()
            return
        }

        if (pin == prefs.pinCode) {
            onSuccess()
        } else {
            Toast.makeText(this, R.string.pin_not_match, Toast.LENGTH_SHORT).show()
            binding.pinView.clearPin()
        }
    }

    private fun onSuccess() {
        if (isBack) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            openApplication()
        }
    }

    private fun openApplication() {
        startActivity(Intent(this@PinLoginActivity, NavUtil.homeScreen(prefs)))
        finish()
    }

    override fun onReady(context: Context, fingerprintUiHelper: FingerprintHelper) {
        this.fingerprintHelper = fingerprintUiHelper
        if (fingerprintUiHelper.canUseFinger(this)) {
            binding.fingerIcon.visibility = View.VISIBLE
            fingerprintUiHelper.startListening(null)
        } else {
            binding.fingerIcon.visibility = View.GONE
        }
    }

    override fun onFailToCreate() {
        binding.fingerIcon.visibility = View.GONE
    }

    override fun onIdle() {
        binding.fingerIcon.setImageResource(R.drawable.ic_twotone_fingerprint_secondary)
    }

    override fun onAuthenticated() {
        binding.fingerIcon.setImageResource(R.drawable.ic_twotone_fingerprint_green)
        onSuccess()
    }

    override fun onError() {
        binding.fingerIcon.setImageResource(R.drawable.ic_twotone_fingerprint_red)
    }

    override fun onDestroy() {
        super.onDestroy()
        fingerprintHelper?.stopListening()
    }

    companion object {
        const val ARG_BACK = "arg_back"
        const val REQ_CODE = 1233

        fun verify(activity: Activity, code: Int = REQ_CODE) {
            activity.startActivityForResult(Intent(activity, PinLoginActivity::class.java)
                    .putExtra(ARG_BACK, true), code)
        }
    }
}
