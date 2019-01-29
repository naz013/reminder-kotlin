package com.elementary.tasks.core

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.*
import javax.inject.Inject

abstract class ThemedActivity : AppCompatActivity() {

    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var language: Language
    @Inject
    lateinit var dialogues: Dialogues
    @Inject
    lateinit var notifier: Notifier
    var isDark = false
        private set

    init {
        ReminderApp.appComponent.inject(this)
    }

    protected open fun applyTheme(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (applyTheme()) {
            setTheme(themeUtil.styleWithAccent)
        }
        isDark = themeUtil.isDark
    }

    override fun onStart() {
        super.onStart()
        if (Module.isChromeOs(this)) {
            window.statusBarColor = themeUtil.getSecondaryColor()
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
