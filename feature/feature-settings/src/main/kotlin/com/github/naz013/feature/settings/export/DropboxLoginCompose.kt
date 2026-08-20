package com.github.naz013.feature.settings.export

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
internal fun rememberDropboxLogin(): DropboxLogin {
  val context = LocalContext.current
  return koinInject<DropboxLogin> { parametersOf(context) }
}
