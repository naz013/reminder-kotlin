package com.elementary.tasks.pin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.os.BiometricProvider
import com.elementary.tasks.databinding.ActivityPinLoginBinding
import com.elementary.tasks.home.BottomNavActivity

class PinLoginActivity : BindingActivity<ActivityPinLoginBinding>(), AuthFragment.AuthCallback {

  private val biometricProvider = BiometricProvider(this) { onSuccess() }

  private var isBack = false
  private var hasFinger = false

  override fun inflateBinding() = ActivityPinLoginBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isBack = intent.getBooleanExtra(ARG_BACK, false)
    hasFinger = prefs.useFingerprint

    openPinLogin()
    if (hasFinger) {
      biometricProvider.tryToOpenFingerLogin()
    }
  }

  private fun openPinLogin() {
    runCatching {
      supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, PinFragment.newInstance(hasFinger), null)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .commitAllowingStateLoss()
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
      AuthFragment.AUTH_FINGER -> biometricProvider.tryToOpenFingerLogin()
      else -> {
      }
    }
  }

  private fun openApplication() {
    startActivity(Intent(this, BottomNavActivity::class.java))
    finish()
  }

  override fun handleBackPress(): Boolean {
    setResult(Activity.RESULT_CANCELED)
    finishAffinity()
    return true
  }

  companion object {
    const val ARG_BACK = "arg_back"
    const val ARG_LOGGED = "arg_logged"

    fun loggedIntent(intent: Intent): Intent {
      intent.putExtra(ARG_LOGGED, true)
      return intent
    }

    fun openLogged(context: Context, intent: Intent) {
      intent.putExtra(ARG_LOGGED, true)
      context.startActivity(intent)
    }

    fun openLogged(context: Context, clazz: Class<*>) {
      context.startActivity(Intent(context, clazz)
        .putExtra(ARG_LOGGED, true))
    }
  }
}
