package com.github.naz013.ui.common.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

interface AuthProvider {
  fun requestAuth(
    onAuthSuccess: () -> Unit,
    onAuthFailure: () -> Unit = {},
  )
}

@Composable
fun rememberAuthProvider(): AuthProvider {
  val context = LocalContext.current

  val data = remember { mutableStateOf<AuthData?>(null) }

  val pinLoginLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        data.value?.onAuthSuccess()
      } else {
        data.value?.onAuthFailure()
      }
      data.value = null
    }

  return object : AuthProvider {
    override fun requestAuth(onAuthSuccess: () -> Unit, onAuthFailure: () -> Unit) {
      data.value = AuthData(onAuthSuccess, onAuthFailure)
      pinLoginLauncher.launch(LoginApi.authIntent(context))
    }
  }
}

private data class AuthData(
  val onAuthSuccess: () -> Unit = {},
  val onAuthFailure: () -> Unit = {},
)
