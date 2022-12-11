package com.elementary.tasks.core.cloud

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.tasks.TasksScopes
import timber.log.Timber

class GoogleLogin(
  private val fragment: BaseFragment<*>,
  private val prefs: Prefs,
  private val drive: GDrive,
  private val tasks: GTasks,
  private val loginCallback: LoginCallback
) {

  var isGoogleDriveLogged = false
    private set
    get() {
      return drive.isLogged
    }

  var isGoogleTasksLogged = false
    private set
    get() {
      return tasks.isLogged
    }

  private var mode = Mode.DRIVE
  private val resultLauncher = fragment.registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    onActivityResult(result.resultCode, result.data)
  }

  fun logOutDrive() {
    mode = Mode.DRIVE

    drive.logOut()

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
    client.signOut().addOnSuccessListener {
      loginCallback.onResult(false, mode)
    }
  }

  fun logOutTasks() {
    mode = Mode.TASKS

    tasks.logOut()

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(TasksScopes.TASKS))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
    client.signOut().addOnSuccessListener {
      loginCallback.onResult(false, mode)
    }
  }

  fun loginDrive() {
    mode = Mode.DRIVE

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
    resultLauncher.launch(client.signInIntent)
  }

  fun loginTasks() {
    mode = Mode.TASKS

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(TasksScopes.TASKS))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(fragment.requireContext(), signInOptions)
    resultLauncher.launch(client.signInIntent)
  }

  private fun sendFail() {
    loginCallback.onFail(mode)
  }

  private fun onActivityResult(resultCode: Int, data: Intent?) {
    Timber.d("onActivityResult: mode=${mode}, res=$resultCode, data=$data")
    if (resultCode == RESULT_OK) {
      if (data != null) handleSignInResult(data)
      else sendFail()
    } else {
      sendFail()
    }
  }

  private fun handleSignInResult(result: Intent) {
    GoogleSignIn.getSignedInAccountFromIntent(result)
      .addOnSuccessListener { googleAccount ->
        Timber.d("Signed in as ${googleAccount.email}")
        finishLogin(googleAccount.account?.name ?: "")
      }
      .addOnFailureListener {
        Timber.d("Sign in fail: ${it.message}")
        sendFail()
      }
  }

  private fun finishLogin(account: String) {
    Timber.d("finishLogin: mode=$mode, $account")
    if (account.isEmpty()) {
      sendFail()
      return
    }
    if (mode == Mode.DRIVE) {
      drive.logOut()
      prefs.driveUser = account
      drive.statusCallback = object : GDrive.StatusCallback {
        override fun onStatusChanged(isLogged: Boolean) {
          loginCallback.onResult(isLogged, mode)
        }
      }
      drive.login(account)
    } else {
      tasks.logOut()
      tasks.statusCallback = object : GTasks.StatusCallback {
        override fun onStatusChanged(isLogged: Boolean) {
          loginCallback.onResult(isLogged, mode)
        }
      }
      prefs.tasksUser = account
      tasks.login(account)
    }
  }

  interface LoginCallback {
    fun onProgress(isLoading: Boolean, mode: Mode)

    fun onResult(isLogged: Boolean, mode: Mode)

    fun onFail(mode: Mode)
  }

  enum class Mode { DRIVE, TASKS }
}
