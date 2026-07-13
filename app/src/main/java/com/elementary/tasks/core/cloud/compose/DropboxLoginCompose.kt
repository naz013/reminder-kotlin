package com.elementary.tasks.core.cloud.compose

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elementary.tasks.core.cloud.DropboxLogin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun rememberDropboxLogin(onResult: (Boolean) -> Unit): DropboxLogin {
  val activity = LocalActivity.current as Activity
  val callback =
    remember(onResult) {
      object : DropboxLogin.LoginCallback {
        override fun onResult(isSuccess: Boolean) = onResult(isSuccess)
      }
    }
  return koinInject<DropboxLogin> { parametersOf(activity, callback) }
}
