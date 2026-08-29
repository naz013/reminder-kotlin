package com.github.naz013.feature.settings.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.common.googleauth.GoogleAuthorizationState
import com.github.naz013.common.googleauth.rememberGoogleAuthorization
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import org.koin.compose.koinInject

/**
 * Google Drive sign-in state, tied to whichever composable calls [rememberGoogleDriveLogin] - the
 * Drive counterpart of [com.github.naz013.ui.googletask.rememberGoogleTasksLogin]. Replaces the
 * old Fragment-hosted Drive login's approach, which has no Fragment to eagerly register an
 * `ActivityResultLauncher` in `onCreate()` against.
 */
internal class GoogleDriveLoginState internal constructor(
  private val authorization: GoogleAuthorizationState,
  private val googleDriveApi: GoogleDriveApi,
  private val googleDriveAuthManager: GoogleDriveAuthManager,
) {
  val isLogged: Boolean get() = googleDriveAuthManager.isAuthorized()

  fun login() {
    authorization.authorize(googleDriveAuthManager.getScopes())
  }

  fun logOut(onResult: (Boolean) -> Unit) {
    googleDriveApi.disconnect()
    googleDriveAuthManager.saveUserName("")
    // The account name is already cleared above regardless of what clearCredentialState() does,
    // so the UI must reflect "logged out" even if that call fails (e.g. no network) - otherwise
    // the Log out button looks like it did nothing even though the app-level session was in fact
    // cleared.
    authorization.signOut { onResult(false) }
  }
}

@Composable
internal fun rememberGoogleDriveLogin(
  onResult: (Boolean) -> Unit,
  onFail: () -> Unit = {},
): GoogleDriveLoginState {
  val googleDriveApi = koinInject<GoogleDriveApi>()
  val googleDriveAuthManager = koinInject<GoogleDriveAuthManager>()
  val scheduleBackgroundWorkUseCase = koinInject<ScheduleBackgroundWorkUseCase>()
  val buildInfo = koinInject<BuildInfo>()

  val authorization = rememberGoogleAuthorization(
    serverClientId = buildInfo.googleSignInServerClientId,
    onResult = { email ->
      googleDriveApi.disconnect()
      googleDriveAuthManager.saveUserName(email)
      val success = googleDriveApi.initialize()
      onResult(success)
      if (success) {
        scheduleBackgroundWorkUseCase(workType = WorkType.ForceSync, dataType = null, id = null, ids = null)
      }
    },
    onFail = onFail,
  )

  return remember(authorization, googleDriveApi, googleDriveAuthManager) {
    GoogleDriveLoginState(
      authorization = authorization,
      googleDriveApi = googleDriveApi,
      googleDriveAuthManager = googleDriveAuthManager,
    )
  }
}
