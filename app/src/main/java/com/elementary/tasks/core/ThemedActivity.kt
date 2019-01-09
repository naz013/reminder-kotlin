package com.elementary.tasks.core

import android.content.Context
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(themeUtil.styleWithAccent)
        isDark = themeUtil.isDark
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(language.onAttach(newBase))
    }
}
