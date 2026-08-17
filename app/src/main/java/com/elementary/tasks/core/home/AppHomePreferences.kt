package com.elementary.tasks.core.home

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.home.HomePreferences

/**
 * `feature-home` can't depend on `app`, so this wraps the home-related subset of app's monolithic
 * `Prefs` SharedPreferences store behind [HomePreferences] instead.
 */
class AppHomePreferences(
  private val prefs: Prefs,
) : HomePreferences {
  override var isUserLogged: Boolean
    get() = prefs.isUserLogged
    set(value) { prefs.isUserLogged = value }
  override var lastVersionCode: Long
    get() = prefs.lastVersionCode
    set(value) { prefs.lastVersionCode = value }
  override val birthdayColor: Int
    get() = prefs.birthdayColor
}
