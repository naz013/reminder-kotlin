package com.elementary.tasks.navigation

import com.elementary.tasks.splash.ShortcutDestination.Shortcut
import com.github.naz013.feature.pomodoro.PomodoroNavKey
import com.github.naz013.feature.routine.RoutineNavKey
import com.github.naz013.feature.settings.SettingsNavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class ShortcutRouteResolverTest {

  @Test
  fun `routes focus shortcut to pro version screen when not pro`() {
    val keys = ShortcutRouteResolver.resolve(Shortcut.StartFocus, null, isPro = false, routineExists = true)

    assertEquals(listOf(SettingsNavKey.ProVersion), keys)
  }

  @Test
  fun `routes focus shortcut to timer when pro`() {
    val keys = ShortcutRouteResolver.resolve(Shortcut.StartFocus, null, isPro = true, routineExists = true)

    assertEquals(listOf(PomodoroNavKey.Timer()), keys)
  }

  @Test
  fun `routes unknown routine to home`() {
    val keys = ShortcutRouteResolver.resolve(Shortcut.Routine, "x", isPro = true, routineExists = false)

    assertEquals(emptyList<Any>(), keys)
  }

  @Test
  fun `routes known routine to execute screen`() {
    val keys = ShortcutRouteResolver.resolve(Shortcut.Routine, "x", isPro = true, routineExists = true)

    assertEquals(listOf(RoutineNavKey.List, RoutineNavKey.Execute("x")), keys)
  }
}
