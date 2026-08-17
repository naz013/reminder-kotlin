package com.github.naz013.ui.googletask

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.logging.Logger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.compose.koinInject

private const val TAG = "GoogleTasksLogin"

/**
 * Google Tasks sign-in state, tied to whichever composable calls [rememberGoogleTasksLogin] -
 * unlike [com.elementary.tasks.core.cloud.GoogleLogin] (still used by the legacy Cloud Backup
 * settings screens for Drive), this doesn't need a Fragment-level "currently active screen"
 * indirection: [rememberLauncherForActivityResult] can be registered lazily from inside the one
 * screen (the Google Tasks list) that actually shows a "Connect" button, scoped to its own
 * composition lifecycle.
 */
class GoogleTasksLoginState internal constructor(
  private val launcher: ActivityResultLauncher<Intent>,
  private val signInClient: () -> GoogleSignInClient,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
) {
  val isLogged: Boolean get() = googleTasksAuthManager.isAuthorized()

  fun login() {
    launcher.launch(signInClient().signInIntent)
  }
}

/**
 * Compose replacement for [com.elementary.tasks.core.cloud.GoogleLogin] scoped to Google Tasks
 * sign-in only (Drive sign-in stays on the legacy class for now). See
 * [com.elementary.tasks.core.os.datapicker.compose.rememberContactPhonePicker] for the same
 * `rememberLauncherForActivityResult`-based pattern.
 */
@Composable
fun rememberGoogleTasksLogin(
  onResult: (Boolean) -> Unit,
  onFail: () -> Unit = {},
): GoogleTasksLoginState {
  val context = LocalContext.current
  val googleTasksApi = koinInject<GoogleTasksApi>()
  val googleTasksAuthManager = koinInject<GoogleTasksAuthManager>()

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      Logger.d(TAG, "processResult: res=${result.resultCode}, data=${result.data}")
      val data = result.data
      if (result.resultCode != Activity.RESULT_OK || data == null) {
        onFail()
        return@rememberLauncherForActivityResult
      }
      GoogleSignIn
        .getSignedInAccountFromIntent(data)
        .addOnSuccessListener { account ->
          val name = account.account?.name ?: ""
          if (name.isEmpty()) {
            onFail()
            return@addOnSuccessListener
          }
          googleTasksApi.disconnect()
          googleTasksAuthManager.saveUserName(name)
          onResult(googleTasksApi.initialize())
        }.addOnFailureListener {
          Logger.d(TAG, "Sign in fail: ${it.message}")
          onFail()
        }
    }

  return remember(launcher, context, googleTasksApi, googleTasksAuthManager) {
    GoogleTasksLoginState(
      launcher = launcher,
      signInClient = {
        val scopes = googleTasksAuthManager.getScopes().map { Scope(it) }
        val signInOptions =
          GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(scopes.first(), *scopes.drop(1).toTypedArray())
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, signInOptions)
      },
      googleTasksAuthManager = googleTasksAuthManager,
    )
  }
}

@Composable
fun rememberGoogleTasksAuthManager(): GoogleTasksAuthManager = koinInject()
