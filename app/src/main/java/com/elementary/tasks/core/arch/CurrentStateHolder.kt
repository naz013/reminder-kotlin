package com.elementary.tasks.core.arch

import android.content.Context
import com.github.naz013.ui.common.locale.Language
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.common.theme.ThemeProvider

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
