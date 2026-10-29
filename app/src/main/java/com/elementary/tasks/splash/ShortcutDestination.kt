package com.elementary.tasks.splash

import android.os.Bundle

object ShortcutDestination {
  private const val KEY_HAS_SHORTCUT = "key_has_shortcut"
  private const val KEY_SHORTCUT_TYPE = "key_shortcut_type"
  private const val KEY_ROUTINE_ID = "key_shortcut_routine_id"

  fun hasShortcut(bundle: Bundle?): Boolean = bundle?.getBoolean(KEY_HAS_SHORTCUT, false) ?: false

  fun getShortcut(bundle: Bundle?): Shortcut? =
    bundle
      ?.getString(KEY_SHORTCUT_TYPE)
      ?.let { Shortcut.valueOf(it) }

  fun getRoutineId(bundle: Bundle?): String? = bundle?.getString(KEY_ROUTINE_ID)

  fun createBundle(
    shortcut: Shortcut,
    bundle: Bundle? = null,
  ): Bundle =
    (bundle?.let { Bundle(it) } ?: Bundle()).apply {
      putBoolean(KEY_HAS_SHORTCUT, true)
      putString(KEY_SHORTCUT_TYPE, shortcut.name)
    }

  fun createRoutineBundle(routineId: String): Bundle =
    createBundle(Shortcut.Routine, Bundle().apply { putString(KEY_ROUTINE_ID, routineId) })

  enum class Shortcut {
    Reminder,
    Note,
    GoogleTask,
    QuickAdd,
    StartFocus,
    Routine,
  }
}
