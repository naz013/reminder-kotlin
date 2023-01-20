package com.elementary.tasks.core.arch

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.Serializable

abstract class ThemedActivity : AppCompatActivity() {

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val language = currentStateHolder.language
  protected val dialogues by inject<Dialogues>()
  protected val notifier by inject<Notifier>()
  protected val updatesHelper by inject<UpdatesHelper>()

  private val loginStateViewModel by viewModel<LoginStateViewModel>()

  private val loginLauncher = LoginLauncher(this) {
    loginStateViewModel.isLogged = it
    if (!it) {
      finish()
    }
  }

  private val uiHandler = Handler(Looper.getMainLooper())
  protected val isDarkMode = currentStateHolder.theme.isDark

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
    logId()
  }

  private fun logId() {
    if (intent.hasExtra(Constants.INTENT_ID)) {
      Timber.d("Has ID as ${intent.getStringExtra(Constants.INTENT_ID)}")
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
    if (requireLogin() && prefs.hasPinCode && !loginStateViewModel.isLogged) {
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

  protected fun isLogged() = intentBoolean(PinLoginActivity.ARG_LOGGED)

  protected fun intentString(key: String, def: String = "") = intent.getStringExtra(key) ?: def

  protected fun intentLong(key: String, def: Long = 0) = intent.getLongExtra(key, def)

  protected fun intentBoolean(key: String, def: Boolean = false) = intent.getBooleanExtra(key, def)

  protected fun <T> intentParcelable(key: String, clazz: Class<T>): T? {
    return runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(key, clazz)
      } else {
        intent.getParcelableExtra(key) as? T
      }
    }.getOrNull()
  }

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
}
