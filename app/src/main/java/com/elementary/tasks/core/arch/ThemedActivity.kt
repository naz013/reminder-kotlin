package com.elementary.tasks.core.arch

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.core.utils.*
import org.koin.android.ext.android.inject

abstract class ThemedActivity : AppCompatActivity() {

    protected val prefs: Prefs by inject()
    protected val language: Language by inject()
    protected val dialogues: Dialogues by inject()
    protected val notifier: Notifier by inject()

    protected var isDarkMode = false
        private set

    @Deprecated("Not used anymore", ReplaceWith("true"))
    protected open fun applyTheme(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
        isDarkMode = ThemeUtil.isDarkMode(this)
    }

    override fun onStart() {
        super.onStart()
        if (Module.isChromeOs(this)) {
            window.statusBarColor = ThemeUtil.getSecondaryColor(this)
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
