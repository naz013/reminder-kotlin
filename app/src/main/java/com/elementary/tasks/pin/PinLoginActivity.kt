package com.elementary.tasks.pin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ActivityPinLoginBinding
import com.elementary.tasks.experimental.NavUtil

class PinLoginActivity : BindingActivity<ActivityPinLoginBinding>(R.layout.activity_pin_login),
        AuthFragment.AuthCallback{

    private var isBack = false
    private var hasFinger = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBack = intent.getBooleanExtra(ARG_BACK, false)
        hasFinger = prefs.useFingerprint && Module.isMarshmallow && Module.hasFingerprint(this)

        if (hasFinger) {
            openFingerLogin()
        } else {
            openPinLogin()
        }
    }

    private fun openPinLogin() {
        try {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PinFragment.newInstance(hasFinger), null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    private fun openFingerLogin() {
        try {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FingerFragment.newInstance(), null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    override fun onSuccess() {
        if (isBack) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            openApplication()
        }
    }

    override fun changeScreen(auth: Int) {
        when (auth) {
            AuthFragment.AUTH_FINGER -> openFingerLogin()
            else -> openPinLogin()
        }
    }

    private fun openApplication() {
        startActivity(Intent(this@PinLoginActivity, NavUtil.homeScreen(prefs)))
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finishAffinity()
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
