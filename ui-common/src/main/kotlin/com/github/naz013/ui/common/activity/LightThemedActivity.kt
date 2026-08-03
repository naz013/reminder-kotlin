package com.github.naz013.ui.common.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.system.Module
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.login.AuthPreferences
import com.github.naz013.ui.common.login.LoginApi.isLogged
import com.github.naz013.ui.common.login.LoginLauncher
import com.github.naz013.ui.common.login.LoginStateViewModel
import com.github.naz013.ui.common.theme.ThemePreferences
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class LightThemedActivity : AppCompatActivity() {

  private val themeProvider by inject<ThemeProvider>()
  private val themePreferences by inject<ThemePreferences>()
  protected val language by inject<Language>()
  private val authPreferences by inject<AuthPreferences>()

  private val loginStateViewModel by viewModel<LoginStateViewModel>()

  private val loginLauncher = LoginLauncher(this) {
    loginStateViewModel.isLogged = it
    if (!it) {
      finish()
    }
  }

  protected val isDarkMode: Boolean
    get() {
      return themeProvider.isDark
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
      onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            invokeBackPress()
          }
        }
      )
    }
    logId()
  }

  private fun logId() {
    if (intent.hasExtra(IntentKeys.INTENT_ID)) {
      Logger.d(TAG, "Has ID as ${intent.getStringExtra(IntentKeys.INTENT_ID)}")
    }
  }

  fun invokeBackPress() {
    Logger.i(TAG, "Back pressed from callback")
    if (!handleBackPress()) finish()
  }

  override fun onStart() {
    super.onStart()
    if (Module.isChromeOs(this)) {
      window.statusBarColor = ThemeProvider.getPrimaryColor(this)
    }
    if (requireLogin() && authPreferences.hasPinCode && !loginStateViewModel.isLogged) {
      loginLauncher.askLogin()
    }
  }

  override fun onResume() {
    super.onResume()
    AppCompatDelegate.setDefaultNightMode(themePreferences.nightMode)
    if (themePreferences.useDynamicColors) {
      DynamicColors.applyToActivityIfAvailable(this)
    }
  }

  override fun onRestart() {
    super.onRestart()
    AppCompatDelegate.setDefaultNightMode(themePreferences.nightMode)
    if (themePreferences.useDynamicColors) {
      DynamicColors.applyToActivityIfAvailable(this)
    }
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(language.onAttach(newBase))
  }

  protected fun hideKeyboard(token: IBinder? = null) {
    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
    if (token == null) {
      val currentToken = window.currentFocus?.windowToken
      currentToken?.let { imm?.hideSoftInputFromWindow(token, 0) }
    } else {
      imm?.hideSoftInputFromWindow(token, 0)
    }
  }

  protected fun isLogged() = intent.isLogged()

  protected fun intentString(key: String, def: String = "") = intent.getStringExtra(key) ?: def

  open fun requireLogin() = false

  protected open fun handleBackPress(): Boolean {
    return false
  }

  companion object {
    private const val TAG = "LightThemedActivity"
  }
}
