package com.elementary.tasks.core.arch

import android.content.Context
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ThemeProvider

class CurrentStateHolder(
  prefs: Prefs,
  themeProvider: ThemeProvider,
  val language: Language,
  val context: Context,
  val notifier: Notifier
) {
  val preferences = prefs
  val theme = themeProvider
}