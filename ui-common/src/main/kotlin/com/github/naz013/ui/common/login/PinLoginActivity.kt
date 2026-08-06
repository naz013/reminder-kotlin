package com.github.naz013.ui.common.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class PinLoginActivity : ComponentActivity() {

  private val authPreferences by inject<AuthPreferences>()
  private val navigator by inject<Navigator>()
  private val systemInfo by inject<SystemInfo>()
  private val viewModel by viewModel<PinLoginViewModel>()

  private var isBack = false
  private var hasFinger = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isBack = intent.getBooleanExtra(ARG_BACK, false)
    hasFinger = authPreferences.useFingerprint

    composeView { Content() }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      onBackInvokedDispatcher.registerOnBackInvokedCallback(
        OnBackInvokedDispatcher.PRIORITY_DEFAULT
      ) {
        handleBackPress()
      }
    } else {
      onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            handleBackPress()
          }
        }
      )
    }
  }

  @Composable
  private fun Content() {
    val biometricProvider = rememberBiometricProvider()

    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
      viewModel.navigationEvent.collect { event ->
        when (event) {
          PinLoginEvent.Success -> onSuccess()
          PinLoginEvent.ShowPinMismatch -> toast(R.string.pin_not_match)
        }
      }
    }

    LaunchedEffect(systemInfo) {
      if (systemInfo.hasBiometricHardware && hasFinger) {
        biometricProvider.authenticate(
          onSuccess = { viewModel.onFingerprintSucceeded() }
        )
      }
    }

    Box(
      modifier = Modifier.fillMaxSize(),
    ) {
      PinLoginScreen(
        pin = state.pin,
        shuffleDigits = state.shuffleDigits,
        showFingerprintButton = hasFinger,
        onDigitClick = viewModel::onDigitClick,
        onDeleteClick = viewModel::onDeleteClick,
        onFingerprintClick = {
          biometricProvider.authenticate(
            onSuccess = { viewModel.onFingerprintSucceeded() }
          )
        },
        onCloseClick = { handleBackPress() },
      )
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
    navigator.navigate(
      ActivityDestination(
        screen = DestinationScreen.Main,
        flags = Intent.FLAG_ACTIVITY_NEW_TASK,
      ),
    )
    finish()
  }

  private fun handleBackPress() {
    setResult(Activity.RESULT_CANCELED)
    finishAffinity()
  }

  companion object {
    const val ARG_BACK = "arg_back"
    const val ARG_LOGGED = "arg_logged"
  }
}
