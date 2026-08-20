package com.github.naz013.feature.settings.export

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.compose.koinInject

private const val TAG = "GoogleDriveLogin"

/**
 * Google Drive sign-in state, tied to whichever composable calls [rememberGoogleDriveLogin] - the
 * Drive counterpart of [com.github.naz013.ui.googletask.rememberGoogleTasksLogin]. Replaces the
 * old Fragment-hosted Drive login's approach, which has no Fragment to eagerly register an
 * `ActivityResultLauncher` in `onCreate()` against.
 */
internal class GoogleDriveLoginState internal constructor(
  private val launcher: ActivityResultLauncher<Intent>,
  private val signInClient: () -> GoogleSignInClient,
  private val googleDriveApi: GoogleDriveApi,
  private val googleDriveAuthManager: GoogleDriveAuthManager,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  val isLogged: Boolean get() = googleDriveAuthManager.isAuthorized()

  fun login() {
    launcher.launch(signInClient().signInIntent)
  }

  fun logOut(onResult: (Boolean) -> Unit) {
    googleDriveApi.disconnect()
    googleDriveAuthManager.saveUserName("")
    signInClient().signOut().addOnSuccessListener { onResult(false) }
  }
}

@Composable
internal fun rememberGoogleDriveLogin(
  onResult: (Boolean) -> Unit,
  onFail: () -> Unit = {},
): GoogleDriveLoginState {
  val context = LocalContext.current
  val googleDriveApi = koinInject<GoogleDriveApi>()
  val googleDriveAuthManager = koinInject<GoogleDriveAuthManager>()
  val scheduleBackgroundWorkUseCase = koinInject<ScheduleBackgroundWorkUseCase>()

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
          googleDriveApi.disconnect()
          googleDriveAuthManager.saveUserName(name)
          val success = googleDriveApi.initialize()
          onResult(success)
          if (success) {
            scheduleBackgroundWorkUseCase(workType = WorkType.ForceSync, dataType = null, id = null, ids = null)
          }
        }.addOnFailureListener {
          Logger.d(TAG, "Sign in fail: ${it.message}")
          onFail()
        }
    }

  return remember(launcher, context, googleDriveApi, googleDriveAuthManager, scheduleBackgroundWorkUseCase) {
    GoogleDriveLoginState(
      launcher = launcher,
      signInClient = {
        val scopes = googleDriveAuthManager.getScopes().map { Scope(it) }
        val signInOptions =
          GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(scopes.first(), *scopes.drop(1).toTypedArray())
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, signInOptions)
      },
      googleDriveApi = googleDriveApi,
      googleDriveAuthManager = googleDriveAuthManager,
      scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
    )
  }
}
