package com.elementary.tasks.core.cloud

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.utils.Logger
import com.elementary.tasks.core.utils.Prefs
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.tasks.TasksScopes

class GoogleLogin(
  private val activity: Activity,
  private val prefs: Prefs,
  private val gDrive: GDrive,
  private val gTasks: GTasks
) {

  private var mDriveCallback: LoginCallback? = null
  private var mTasksCallback: LoginCallback? = null
  private var isDriveLogin = false

  var googleStatus: ((Boolean) -> Unit)? = null

  var isGoogleDriveLogged = false
    private set
    get() {
      return gDrive.isLogged
    }

  var isGoogleTasksLogged = false
    private set
    get() {
      return gDrive.isLogged
    }

  fun logOutDrive() {
    gDrive.logOut()

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(activity, signInOptions)
    client.signOut().addOnSuccessListener {
      googleStatus?.invoke(false)
    }
  }

  fun logOutTasks() {
    gTasks.logOut()

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(TasksScopes.TASKS))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(activity, signInOptions)
    client.signOut().addOnSuccessListener {
      googleStatus?.invoke(false)
    }
  }

  fun loginDrive(loginCallback: LoginCallback) {
    isDriveLogin = true
    mDriveCallback = loginCallback

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(activity, signInOptions)
    activity.startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
  }

  fun loginTasks(loginCallback: LoginCallback) {
    isDriveLogin = false
    mTasksCallback = loginCallback

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestScopes(Scope(TasksScopes.TASKS))
      .requestEmail()
      .build()
    val client = GoogleSignIn.getClient(activity, signInOptions)
    activity.startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
  }

  private fun sendFail() {
    if (isDriveLogin) {
      mDriveCallback?.onFail()
    } else {
      mTasksCallback?.onFail()
    }
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    Logger.d("GoogleLogin: onActivityResult: req=$requestCode, res=$resultCode, data=$data")
    if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
      if (data != null) handleSignInResult(data)
      else sendFail()
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
    Logger.d("finishLogin: $account")
    if (account.isEmpty()) {
      sendFail()
      return
    }
    if (isDriveLogin) {
      gDrive.logOut()
      prefs.driveUser = account
      gDrive.statusObserver = {
        googleStatus?.invoke(it)
      }
      mDriveCallback?.onResult(gDrive.isLogged)
    } else {
      gTasks.logOut()
      gTasks.statusObserver = {
        googleStatus?.invoke(it)
      }
      gTasks.login(account)
      mTasksCallback?.onResult(gTasks.isLogged)
    }
  }

  interface LoginCallback {
    fun onProgress(isLoading: Boolean)

    fun onResult(isLogged: Boolean)

    fun onFail()
  }

  companion object {
    private const val REQUEST_CODE_SIGN_IN = 4
  }
}
