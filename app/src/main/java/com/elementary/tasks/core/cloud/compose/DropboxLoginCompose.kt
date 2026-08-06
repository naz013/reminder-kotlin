package com.elementary.tasks.core.cloud.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.core.cloud.DropboxLogin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun rememberDropboxLogin(): DropboxLogin {
  val context = LocalContext.current
  return koinInject<DropboxLogin> { parametersOf(context) }
}
