package com.elementary.tasks.navigation

import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.feature.googletask.GoogleTasksNavKey
import com.github.naz013.feature.note.NotesNavKey
import com.github.naz013.feature.pomodoro.PomodoroNavKey
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.quickadd.QuickAddNavKey
import com.github.naz013.feature.routine.RoutineNavKey
import com.github.naz013.feature.settings.SettingsNavKey

internal object ShortcutRouteResolver {

  fun resolve(
    shortcut: ShortcutDestination.Shortcut?,
    routineId: String?,
    isPro: Boolean,
    routineExists: Boolean,
  ): List<NavKey> =
    when (shortcut) {
      ShortcutDestination.Shortcut.Note -> listOf(NotesNavKey.List, NotesNavKey.Edit())
      ShortcutDestination.Shortcut.GoogleTask -> listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit())
      ShortcutDestination.Shortcut.Reminder -> listOf(BuildReminderNavKey.Main())
      ShortcutDestination.Shortcut.QuickAdd -> listOf(QuickAddNavKey.Sheet)
      ShortcutDestination.Shortcut.StartFocus ->
        listOf(if (isPro) PomodoroNavKey.Timer() else SettingsNavKey.ProVersion)
      ShortcutDestination.Shortcut.Routine ->
        if (routineId != null && routineExists) {
          listOf(RoutineNavKey.List, RoutineNavKey.Execute(routineId))
        } else {
          emptyList()
        }
      null -> emptyList()
    }
}
