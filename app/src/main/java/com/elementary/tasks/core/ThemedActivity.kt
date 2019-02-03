package com.elementary.tasks.core

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.*

abstract class ThemedActivity<B : ViewDataBinding> : AppCompatActivity() {

    var themeUtil: ThemeUtil = ReminderApp.appComponent.themeUtil()
    var prefs: Prefs = ReminderApp.appComponent.prefs()
    var language: Language = ReminderApp.appComponent.language()
    var dialogues: Dialogues = ReminderApp.appComponent.dialogues()
    var notifier: Notifier = ReminderApp.appComponent.notifier()
    protected lateinit var binding: B
    var isDark = false
        private set

    protected open fun applyTheme(): Boolean = true

    @LayoutRes
    abstract fun layoutRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (applyTheme()) {
            setTheme(themeUtil.styleWithAccent)
        }
        isDark = themeUtil.isDark

        if (layoutRes() != 0) {
            binding = DataBindingUtil.setContentView(this, layoutRes())
        }
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
