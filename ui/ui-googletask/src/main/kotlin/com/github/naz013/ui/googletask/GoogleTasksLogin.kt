package com.github.naz013.ui.googletask

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.googleauth.GoogleAuthorizationState
import com.github.naz013.common.googleauth.rememberGoogleAuthorization
import com.github.naz013.common.system.BuildInfo
import org.koin.compose.koinInject

/**
 * Google Tasks sign-in state, tied to whichever composable calls [rememberGoogleTasksLogin] -
 * this doesn't need a Fragment-level "currently active screen" indirection:
 * [rememberGoogleAuthorization] can be registered lazily from inside the one screen (the Google
 * Tasks list) that actually shows a "Connect" button, scoped to its own composition lifecycle.
 */
class GoogleTasksLoginState internal constructor(
  private val authorization: GoogleAuthorizationState,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
) {
  val isLogged: Boolean get() = googleTasksAuthManager.isAuthorized()

  fun login() {
    authorization.authorize(googleTasksAuthManager.getScopes())
  }
}

@Composable
fun rememberGoogleTasksLogin(
  onResult: (Boolean) -> Unit,
  onFail: () -> Unit = {},
): GoogleTasksLoginState {
  val googleTasksApi = koinInject<GoogleTasksApi>()
  val googleTasksAuthManager = koinInject<GoogleTasksAuthManager>()
  val buildInfo = koinInject<BuildInfo>()

  val authorization = rememberGoogleAuthorization(
    serverClientId = buildInfo.googleSignInServerClientId,
    onResult = { email ->
      googleTasksApi.disconnect()
      googleTasksAuthManager.saveUserName(email)
      onResult(googleTasksApi.initialize())
    },
    onFail = onFail,
  )

  return remember(authorization, googleTasksAuthManager) {
    GoogleTasksLoginState(
      authorization = authorization,
      googleTasksAuthManager = googleTasksAuthManager,
    )
  }
}

@Composable
fun rememberGoogleTasksAuthManager(): GoogleTasksAuthManager = koinInject()
