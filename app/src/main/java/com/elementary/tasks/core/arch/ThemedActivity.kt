package com.elementary.tasks.core.arch

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeProvider
import org.koin.android.ext.android.inject

abstract class ThemedActivity : AppCompatActivity() {

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val language = currentStateHolder.language
  protected val dialogues by inject<Dialogues>()

  protected val isDarkMode = currentStateHolder.theme.isDark

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
  }

  override fun onStart() {
    super.onStart()
    if (Module.isChromeOs(this)) {
      window.statusBarColor = ThemeProvider.getSecondaryColor(this)
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
}
