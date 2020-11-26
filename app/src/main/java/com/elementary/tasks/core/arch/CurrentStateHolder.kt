package com.elementary.tasks.core.arch

import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeProvider

class CurrentStateHolder(
  prefs: Prefs,
  themeProvider: ThemeProvider,
  val language: Language
) {
  val preferences = prefs
  val theme = themeProvider
}