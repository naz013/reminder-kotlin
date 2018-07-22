package com.elementary.tasks.core

import android.content.Context
import android.os.Bundle

import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil

import androidx.appcompat.app.AppCompatActivity
import com.elementary.tasks.ReminderApp
import javax.inject.Inject

abstract class ThemedActivity : AppCompatActivity() {

    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(themeUtil.style)
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getColor(themeUtil.colorPrimaryDark())
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Language.onAttach(newBase))
    }
}
