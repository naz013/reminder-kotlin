package com.github.naz013.ui.common.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class PinLoginActivity : LightThemedActivity() {

  private val authPreferences by inject<AuthPreferences>()
  private val navigator by inject<Navigator>()
  private val viewModel by viewModel<PinLoginViewModel>()

  private val biometricProvider = BiometricProvider(this) { viewModel.onFingerprintSucceeded() }

  private var isBack = false
  private var hasFinger = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isBack = intent.getBooleanExtra(ARG_BACK, false)
    hasFinger = authPreferences.useFingerprint

    composeView { Content() }

    if (hasFinger) {
      biometricProvider.tryToOpenFingerLogin()
    }
  }

  @Composable
  private fun Content() {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
      viewModel.navigationEvent.collect { event ->
        when (event) {
          PinLoginEvent.Success -> onSuccess()
          PinLoginEvent.ShowPinMismatch -> toast(R.string.pin_not_match)
        }
      }
    }

    PinLoginScreen(
      pin = state.pin,
      shuffleDigits = state.shuffleDigits,
      showFingerprintButton = hasFinger,
      onDigitClick = viewModel::onDigitClick,
      onDeleteClick = viewModel::onDeleteClick,
      onFingerprintClick = { biometricProvider.tryToOpenFingerLogin() },
      onCloseClick = { invokeBackPress() },
    )
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
    navigator.navigate(
      ActivityDestination(
        screen = DestinationScreen.Main,
        flags = Intent.FLAG_ACTIVITY_NEW_TASK,
      ),
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
  }
}
