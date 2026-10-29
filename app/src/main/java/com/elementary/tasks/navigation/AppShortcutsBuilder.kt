package com.elementary.tasks.navigation

import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.domain.routine.Routine

internal data class AppShortcutSpec(
  val id: String,
  val type: ShortcutDestination.Shortcut,
  val routineId: String? = null,
  val routineTitle: String? = null,
)

internal class AppShortcutsBuilder {

  fun build(
    isPro: Boolean,
    isGoogleTasksEnabled: Boolean,
    isRoutineEnabled: Boolean,
    routines: List<Routine>,
    maxCount: Int,
  ): List<AppShortcutSpec> {
    val specs = mutableListOf<AppShortcutSpec>()
    specs += AppShortcutSpec("id.reminder", ShortcutDestination.Shortcut.Reminder)
    specs += AppShortcutSpec("id.note", ShortcutDestination.Shortcut.Note)
    specs += AppShortcutSpec("id.quick.add", ShortcutDestination.Shortcut.QuickAdd)
    if (isPro) {
      specs += AppShortcutSpec("id.focus", ShortcutDestination.Shortcut.StartFocus)
    }
    if (isRoutineEnabled) {
      routines
        .filter { it.isPinned }
        .forEach {
          specs +=
            AppShortcutSpec(
              id = "id.routine.${it.id}",
              type = ShortcutDestination.Shortcut.Routine,
              routineId = it.id,
              routineTitle = it.title,
            )
        }
    }
    if (isGoogleTasksEnabled) {
      specs += AppShortcutSpec("id.google.tasks", ShortcutDestination.Shortcut.GoogleTask)
    }
    return specs.take(maxCount.coerceAtLeast(0))
  }
}
