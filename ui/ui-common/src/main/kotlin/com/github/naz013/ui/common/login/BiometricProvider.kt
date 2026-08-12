package com.github.naz013.ui.common.login

import android.content.Context
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.AuthenticationResultCallback
import androidx.biometric.BiometricManager
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.github.naz013.ui.common.R

interface BiometricProvider {
  fun hasBiometric(): Boolean
  fun authenticate(
    onSuccess: () -> Unit,
    onError: () -> Unit = {},
  )
}

@Composable
fun rememberBiometricProvider(): BiometricProvider {
  val context = LocalContext.current

  val data = remember { mutableStateOf<Data?>(null) }

  val callback = object : AuthenticationResultCallback {
    override fun onAuthResult(result: AuthenticationResult) {
      if (result.isSuccess()) {
        data.value?.onSuccess()
      } else {
        data.value?.onError()
      }
      data.value = null
    }

    override fun onAuthAttemptFailed() {
      data.value?.onError()
      data.value = null
    }
  }
  val authLauncher = rememberAuthenticationLauncher(
    ContextCompat.getMainExecutor(context),
    callback
  )

  return object : BiometricProvider {
    override fun hasBiometric(): Boolean {
      return BiometricManager.from(context).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK
      ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun authenticate(onSuccess: () -> Unit, onError: () -> Unit) {
      if (hasBiometric()) {
        data.value = Data(onSuccess, onError)
        authLauncher.launch(createPromptInfo(context))
      } else {
        onError()
      }
    }

    private fun createPromptInfo(context: Context): AuthenticationRequest {
      return AuthenticationRequest.Biometric.Builder(context.getString(R.string.app_title))
        .setSubtitle(context.getString(R.string.prompt_info_subtitle))
        .setContent(
          AuthenticationRequest.BodyContent.PlainText(
            context.getString(R.string.prompt_info_description)
          )
        )
        .setIsConfirmationRequired(false)
        .build()
    }
  }
}

private data class Data(
  val onSuccess: () -> Unit,
  val onError: () -> Unit,
)
