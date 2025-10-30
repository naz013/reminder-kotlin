package com.elementary.tasks.core.cloud

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.logging.Logger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

class GoogleLogin(
  private val fragment: BaseNavigationFragment<*>,
  private val googleDriveApi: GoogleDriveApi,
  private val googleDriveAuthManager: GoogleDriveAuthManager,
  private val googleTasksApi: GoogleTasksApi,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val loginCallback: LoginCallback,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

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
  private val resultLauncher = fragment.registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
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

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(firstScope, *restScopes)
      .requestEmail()
      .build()
    return GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
  }

  private fun getGoogleDriveSignInClient(): GoogleSignInClient {
    val scopes = googleDriveAuthManager.getScopes().map { Scope(it) }
    val firstScope = scopes.first()
    val restScopes = scopes.drop(1).toTypedArray()

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(firstScope, *restScopes)
      .requestEmail()
      .build()
    return GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
  }

  private fun sendFail() {
    loginCallback.onFail(mode)
  }

  private fun processResult(resultCode: Int, data: Intent?) {
    Logger.d("processResult: mode=$mode, res=$resultCode, data=$data")
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
    GoogleSignIn.getSignedInAccountFromIntent(result)
      .addOnSuccessListener { googleAccount ->
        Logger.d("Signed in as ${googleAccount.email}")
        finishLogin(googleAccount.account?.name ?: "")
      }
      .addOnFailureListener {
        Logger.d("Sign in fail: ${it.message}")
        sendFail()
      }
  }

  private fun finishLogin(account: String) {
    Logger.d("finishLogin: mode=$mode, $account")
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
            workType = WorkType.ForceUpload,
            dataType = null,
            id = null,
            ids = null,
          )
          scheduleBackgroundWorkUseCase(
            workType = WorkType.Sync,
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
    fun onProgress(isLoading: Boolean, mode: Mode)

    fun onResult(isLogged: Boolean, mode: Mode)

    fun onFail(mode: Mode)
  }

  enum class Mode { DRIVE, TASKS }
}
