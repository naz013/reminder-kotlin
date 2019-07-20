package com.elementary.tasks.navigation.settings.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.FingerInitializer
import com.elementary.tasks.core.utils.FingerprintHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ActivityPinLoginBinding
import com.elementary.tasks.experimental.NavUtil

class PinLoginActivity : BindingActivity<ActivityPinLoginBinding>(R.layout.activity_pin_login),
        FingerInitializer.ReadyListener, FingerprintHelper.Callback {

    private var fingerprintHelper: FingerprintHelper? = null
    private var isBack = false

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
