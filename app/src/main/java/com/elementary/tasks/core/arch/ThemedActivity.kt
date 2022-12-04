package com.elementary.tasks.core.arch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Logger
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

typealias ActivityResultListener = (resultCode: Int, data: Intent?) -> Unit

abstract class ThemedActivity : AppCompatActivity() {

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val language = currentStateHolder.language
  protected val dialogues by inject<Dialogues>()
  protected val notifier by inject<Notifier>()
  protected val updatesHelper by inject<UpdatesHelper>()
  private val loginStateViewModel by viewModel<LoginStateViewModel>()

  private val uiHandler = Handler(Looper.getMainLooper())
  protected val isDarkMode = currentStateHolder.theme.isDark
  private var resultLauncher: ActivityResultLauncher<*>? = null
  private var askedPermission: String = ""
  private var permissionRequestCode: Int = 0

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        permissionGranted(askedPermission, permissionRequestCode)
      } else {
        permissionWasNotGranted(askedPermission, permissionRequestCode)
      }
    }

  protected fun launchForResult(
    intent: Intent,
    activityResultListener: ActivityResultListener
  ) {
    resultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) {
      resultLauncher?.unregister()
      activityResultListener.invoke(it.resultCode, it.data)
    }.also {
      it.launch(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
    if (savedInstanceState == null) {
      loginStateViewModel.isLogged = isLogged()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      onBackInvokedDispatcher.registerOnBackInvokedCallback(
        OnBackInvokedDispatcher.PRIORITY_DEFAULT
      ) {
        invokeBackPress()
      }
    } else {
      onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          invokeBackPress()
        }
      })
    }
  }

  fun invokeBackPress() {
    if (!handleBackPress()) finish()
  }

  override fun onStart() {
    super.onStart()
    if (Module.isChromeOs(this)) {
      window.statusBarColor = ThemeProvider.getSecondaryColor(this)
    }
    if (requireLogin() && prefs.hasPinCode && !loginStateViewModel.isLogged) {
      PinLoginActivity.verify(this)
    }
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(language.onAttach(newBase))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PinLoginActivity.LOGIN_REQUEST_CODE) {
      if (resultCode != Activity.RESULT_OK) {
        finish()
      } else {
        loginStateViewModel.isLogged = true
      }
    }
  }

  protected fun hideKeyboard(token: IBinder? = null) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    if (token == null) {
      val currentToken = window.currentFocus?.windowToken
      currentToken?.let { imm?.hideSoftInputFromWindow(token, 0) }
    } else {
      imm?.hideSoftInputFromWindow(token, 0)
    }
  }

  protected fun loginSuccessful() = loginStateViewModel.isLogged

  protected fun isLogged() = intentBoolean(ARG_LOGIN_FLAG)

  protected fun intentString(key: String, def: String = "") = intent.getStringExtra(key) ?: def

  protected fun intentBoolean(key: String, def: Boolean = false) = intent.getBooleanExtra(key, def)

  open fun requireLogin() = false

  protected fun checkPermission(requestCode: Int, vararg permissions: String) =
    Permissions.checkPermission(
      this,
      requestCode,
      *permissions
    )

  protected fun postUi(action: () -> Unit) {
    uiHandler.post(action)
  }

  protected open fun handleBackPress(): Boolean {
    return false
  }

  protected fun askPermission(permission: String, requestCode: Int = 0) {
    this.askedPermission = permission
    this.permissionRequestCode = requestCode
    when {
      ContextCompat.checkSelfPermission(
        this,
        permission
      ) == PackageManager.PERMISSION_GRANTED -> {
        permissionGranted(permission, permissionRequestCode)
      }

      shouldShowRequestPermissionRationale(permission) -> {
        explainPermission(permission, permissionRequestCode)
      }

      else -> {
        requestPermissionLauncher.launch(permission)
      }
    }
  }

  protected fun showPermissionExplanation(
    permission: String,
    requestCode: Int,
    title: String,
    message: String
  ) {
    dialogues.getMaterialDialog(this)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(R.string.ok) { di, _ ->
        di.dismiss()
        requestPermissionAfterRationale(permission, requestCode)
      }
      .create()
      .show()
  }

  private fun requestPermissionAfterRationale(permission: String, requestCode: Int) {
    if (askedPermission == permission && requestCode == permissionRequestCode) {
      requestPermissionLauncher.launch(permission)
    }
  }

  protected open fun permissionGranted(permission: String, requestCode: Int) {
    Logger.d("Permission granted $permission, code=$requestCode")
  }

  protected open fun permissionWasNotGranted(permission: String, requestCode: Int) {
    Logger.d("Permission Not granted $permission, code=$requestCode")
  }

  protected open fun explainPermission(permission: String, requestCode: Int) {
    Logger.d("Explain $permission, code=$requestCode")
  }

  companion object {
    const val ARG_LOGIN_FLAG = "arg_login_flag"
  }
}
