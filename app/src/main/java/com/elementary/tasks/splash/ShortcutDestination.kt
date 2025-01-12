package com.elementary.tasks.splash

import android.os.Bundle

object ShortcutDestination {

  private const val KEY_HAS_SHORTCUT = "key_has_shortcut"
  private const val KEY_SHORTCUT_TYPE = "key_shortcut_type"

  fun hasShortcut(bundle: Bundle?): Boolean {
    return bundle?.getBoolean(KEY_HAS_SHORTCUT, false) ?: false
  }

  fun getShortcut(bundle: Bundle?): Shortcut? {
    return bundle?.getString(KEY_SHORTCUT_TYPE)
      ?.let { Shortcut.valueOf(it) }
  }

  fun createBundle(
    shortcut: Shortcut,
    bundle: Bundle? = null
  ): Bundle {
    return (bundle?.let { Bundle(it) } ?: Bundle()).apply {
      putBoolean(KEY_HAS_SHORTCUT, true)
      putString(KEY_SHORTCUT_TYPE, shortcut.name)
    }
  }

  enum class Shortcut {
    Reminder,
    Note,
    GoogleTask
  }
}
