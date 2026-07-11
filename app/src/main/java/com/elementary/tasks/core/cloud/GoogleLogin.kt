package com.elementary.tasks.core.cloud

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.logging.Logger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Wraps Google Sign-In for both Drive and Tasks.
 *
 * Must be constructed directly with `GoogleLogin(fragment, callback)` — plain constructor call,
 * never through Koin's lazy `by inject { parametersOf(...) }` — and as early as the host
 * Fragment's `onCreate()`. [Fragment.registerForActivityResult] requires registration before the
 * fragment reaches STARTED; a lazy Koin-injected property is only actually constructed on first
 * read, which from a Composable (e.g. a `LaunchedEffect`) happens well after the fragment is
 * already RESUMED, so registration throws. Resolving dependencies here via [KoinComponent].
 */
class GoogleLogin(
  private val fragment: Fragment,
  private val loginCallback: LoginCallback,
) : KoinComponent {
  private val googleDriveApi by inject<GoogleDriveApi>()
  private val googleDriveAuthManager by inject<GoogleDriveAuthManager>()
  private val googleTasksApi by inject<GoogleTasksApi>()
  private val googleTasksAuthManager by inject<GoogleTasksAuthManager>()
  private val scheduleBackgroundWorkUseCase by inject<ScheduleBackgroundWorkUseCase>()

  var isGoogleDriveLogged = false
    private set
    get() {
      return googleDriveAuthManager.isAuthorized()
    }

  var isGoogleTasksLogged = false
    private set
    get() {
      return googleTasksAuthManager.isAuthorized()
    }

  private var mode = Mode.DRIVE
  private val resultLauncher =
    fragment.registerForActivityResult(
      ActivityResultContracts.StartActivityForResult(),
    ) { processResult(it.resultCode, it.data) }

  fun logOutDrive() {
    mode = Mode.DRIVE

    googleDriveApi.disconnect()
    googleDriveAuthManager.saveUserName("")

    val client = getGoogleDriveSignInClient()
    client.signOut().addOnSuccessListener {
      loginCallback.onResult(false, mode)
    }
  }

  fun logOutTasks() {
    mode = Mode.TASKS

    googleTasksApi.disconnect()
    googleTasksAuthManager.saveUserName("")

    val client = getGoogleTasksSignInClient()
    client.signOut().addOnSuccessListener {
      loginCallback.onResult(false, mode)
    }
  }

  fun loginDrive() {
    mode = Mode.DRIVE
    val client = getGoogleDriveSignInClient()
    resultLauncher.launch(client.signInIntent)
  }

  fun loginTasks() {
    mode = Mode.TASKS
    val client = getGoogleTasksSignInClient()
    resultLauncher.launch(client.signInIntent)
  }

  private fun getGoogleTasksSignInClient(): GoogleSignInClient {
    val scopes = googleTasksAuthManager.getScopes().map { Scope(it) }
    val firstScope = scopes.first()
    val restScopes = scopes.drop(1).toTypedArray()

    val signInOptions =
      GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(firstScope, *restScopes)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
  }

  private fun getGoogleDriveSignInClient(): GoogleSignInClient {
    val scopes = googleDriveAuthManager.getScopes().map { Scope(it) }
    val firstScope = scopes.first()
    val restScopes = scopes.drop(1).toTypedArray()

    val signInOptions =
      GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(firstScope, *restScopes)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
  }

  private fun sendFail() {
    loginCallback.onFail(mode)
  }

  private fun processResult(
    resultCode: Int,
    data: Intent?,
  ) {
    Logger.d(TAG, "processResult: mode=$mode, res=$resultCode, data=$data")
    if (resultCode == RESULT_OK) {
      if (data != null) {
        handleSignInResult(data)
      } else {
        sendFail()
      }
    } else {
      sendFail()
    }
  }

  private fun handleSignInResult(result: Intent) {
    GoogleSignIn
      .getSignedInAccountFromIntent(result)
      .addOnSuccessListener { googleAccount ->
        Logger.d(TAG, "Signed in as ${googleAccount.email}")
        finishLogin(googleAccount.account?.name ?: "")
      }.addOnFailureListener {
        Logger.d(TAG, "Sign in fail: ${it.message}")
        sendFail()
      }
  }

  private fun finishLogin(account: String) {
    Logger.d(TAG, "finishLogin: mode=$mode, $account")
    if (account.isEmpty()) {
      sendFail()
      return
    }
    if (mode == Mode.DRIVE) {
      googleDriveApi.disconnect()
      googleDriveAuthManager.saveUserName(account)
      googleDriveApi.initialize().also {
        loginCallback.onResult(it, mode)
        if (it) {
          scheduleBackgroundWorkUseCase(
            workType = WorkType.ForceSync,
            dataType = null,
            id = null,
            ids = null,
          )
        }
      }
    } else {
      googleTasksApi.disconnect()
      googleTasksAuthManager.saveUserName(account)
      loginCallback.onResult(googleTasksApi.initialize(), mode)
    }
  }

  interface LoginCallback {
    fun onProgress(
      isLoading: Boolean,
      mode: Mode,
    )

    fun onResult(
      isLogged: Boolean,
      mode: Mode,
    )

    fun onFail(mode: Mode)
  }

  enum class Mode { DRIVE, TASKS }

  companion object {
    private const val TAG = "GoogleLogin"
  }
}
