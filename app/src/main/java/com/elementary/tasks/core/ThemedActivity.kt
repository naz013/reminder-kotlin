package com.elementary.tasks.core

import android.content.Context
import android.os.Bundle

import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil

import androidx.appcompat.app.AppCompatActivity

abstract class ThemedActivity : AppCompatActivity() {

    protected lateinit var themeUtil: ThemeUtil
        private set
    protected lateinit var prefs: Prefs
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs.getInstance(this)
        themeUtil = ThemeUtil.getInstance(this)
        setTheme(themeUtil.style)
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getColor(themeUtil.colorPrimaryDark())
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Language.onAttach(newBase))
    }
}
