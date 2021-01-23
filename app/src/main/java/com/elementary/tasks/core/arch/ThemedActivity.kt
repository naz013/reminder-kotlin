package com.elementary.tasks.core.arch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Module
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
  private val loginStateViewModel by viewModel<LoginStateViewModel>()

  protected val isDarkMode = currentStateHolder.theme.isDark
  private var resultLauncher: ActivityResultLauncher<*>? = null

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

  companion object {
    const val ARG_LOGIN_FLAG = "arg_login_flag"
  }
}
