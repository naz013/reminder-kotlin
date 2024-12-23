package com.github.naz013.ui.common.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.navigation.ActivityClass
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.activity.DeepLinkData
import com.github.naz013.ui.common.context.intentForClass
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.databinding.ActivityPinLoginBinding
import org.koin.android.ext.android.inject

internal class PinLoginActivity :
  BindingActivity<ActivityPinLoginBinding>(),
  AuthFragment.AuthCallback {

  private val authPreferences by inject<AuthPreferences>()
  private val navigator by inject<Navigator>()

  private val biometricProvider = BiometricProvider(this) { onSuccess() }

  private var isBack = false
  private var hasFinger = false

  override fun inflateBinding() = ActivityPinLoginBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isBack = intent.getBooleanExtra(ARG_BACK, false)
    hasFinger = authPreferences.useFingerprint

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
    navigator.navigate(
      ActivityDestination(
        activityClass = ActivityClass.Main
      )
    )
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

    fun openLogged(context: Context, clazz: Class<*>) {
      context.startActivity(clazz) {
        putExtra(ARG_LOGGED, true)
      }
    }

    fun openLogged(
      context: Context,
      clazz: Class<*>,
      deepLinkData: DeepLinkData,
      builder: Intent.() -> Unit
    ) {
      val intent = context.intentForClass(clazz).apply {
        builder(this)
        putExtra(ARG_LOGGED, true)
        putExtra(IntentKeys.INTENT_DEEP_LINK, true)
        putExtra(deepLinkData.intentKey, deepLinkData)
      }
      context.startActivity(intent)
    }

    fun openLogged(context: Context, clazz: Class<*>, builder: Intent.() -> Unit) {
      val intent = context.intentForClass(clazz).apply {
        builder(this)
        putExtra(ARG_LOGGED, true)
      }
      context.startActivity(intent)
    }
  }
}
