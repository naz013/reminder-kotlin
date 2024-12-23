package com.github.naz013.ui.common.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.github.naz013.common.Module
import com.github.naz013.common.intent.IntentKeys
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
import java.io.Serializable

abstract class LightThemedActivity : FragmentActivity() {

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

  private val uiHandler = Handler(Looper.getMainLooper())
  protected val isDarkMode = themeProvider.isDark

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(themePreferences.nightMode)
    if (themePreferences.useDynamicColors) {
      DynamicColors.applyToActivityIfAvailable(this)
    }
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
        owner = this,
        onBackPressedCallback = object : OnBackPressedCallback(true) {
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
      Logger.d("Has ID as ${intent.getStringExtra(IntentKeys.INTENT_ID)}")
    }
  }

  fun invokeBackPress() {
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

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(language.onAttach(newBase))
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

  protected fun isLogged() = intent.isLogged()

  protected fun intentString(key: String, def: String = "") = intent.getStringExtra(key) ?: def

  protected fun intentLong(key: String, def: Long = 0) = intent.getLongExtra(key, def)

  protected fun intentBoolean(key: String, def: Boolean = false) = intent.getBooleanExtra(key, def)

  protected fun <T : Serializable> intentSerializable(key: String, clazz: Class<T>): T? {
    return runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(key, clazz)
      } else {
        intent.getSerializableExtra(key) as? T
      }
    }.getOrNull()
  }

  open fun requireLogin() = false

  protected fun postUi(action: () -> Unit) {
    uiHandler.post(action)
  }

  protected open fun handleBackPress(): Boolean {
    return false
  }

  protected fun updateStatusBar(view: View, isLightStatusBar: Boolean) {
    val controller = WindowInsetsControllerCompat(window, view)
    controller.isAppearanceLightStatusBars = isLightStatusBar
  }
}
