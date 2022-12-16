package com.elementary.tasks.pin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ActivityPinLoginBinding
import com.elementary.tasks.home.BottomNavActivity

class PinLoginActivity : BindingActivity<ActivityPinLoginBinding>(), AuthFragment.AuthCallback {

  private var isBack = false
  private var hasFinger = false
  private lateinit var biometricPrompt: BiometricPrompt

  override fun inflateBinding() = ActivityPinLoginBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isBack = intent.getBooleanExtra(ARG_BACK, false)
    hasFinger = prefs.useFingerprint && Module.hasBiometric(this)

    openPinLogin()
    if (hasFinger) {
      biometricPrompt = createBiometricPrompt()
      openFingerLogin()
    }
  }

  private fun createBiometricPrompt(): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(this)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }
    }

    return BiometricPrompt(this, executor, callback)
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
    if (BiometricManager.from(this)
        .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
      val promptInfo = createPromptInfo()
      biometricPrompt.authenticate(promptInfo)
    }
  }

  private fun createPromptInfo(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
      .setTitle(getString(R.string.app_title))
      .setSubtitle(getString(R.string.prompt_info_subtitle))
      .setDescription(getString(R.string.prompt_info_description))
      // Authenticate without requiring the user to press a "confirm"
      // button after satisfying the biometric check
      .setConfirmationRequired(false)
      .setNegativeButtonText(getString(R.string.enter_your_pin))
      .build()
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
      else -> {
      }
    }
  }

  private fun openApplication() {
    startActivity(Intent(this@PinLoginActivity, BottomNavActivity::class.java))
    finish()
  }

  override fun handleBackPress(): Boolean {
    setResult(Activity.RESULT_CANCELED)
    finishAffinity()
    return true
  }

  companion object {
    const val ARG_BACK = "arg_back"
    const val LOGIN_REQUEST_CODE = 1233

    @Deprecated("Use LoginLauncher")
    fun verify(activity: Activity, code: Int = LOGIN_REQUEST_CODE) {
      activity.startActivityForResult(Intent(activity, PinLoginActivity::class.java)
        .putExtra(ARG_BACK, true), code)
    }
  }
}
