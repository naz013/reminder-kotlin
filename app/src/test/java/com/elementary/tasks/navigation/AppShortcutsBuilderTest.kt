package com.elementary.tasks.navigation

import com.elementary.tasks.splash.ShortcutDestination.Shortcut
import com.github.naz013.domain.routine.Routine
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class AppShortcutsBuilderTest {
  private val builder = AppShortcutsBuilder()
  private val now = LocalDateTime.of(2026, 1, 1, 0, 0)
  private val routines =
    listOf(
      Routine(id = "a", title = "A", isPinned = true, createdAt = now, updatedAt = now),
      Routine(id = "b", title = "B", isPinned = false, createdAt = now, updatedAt = now),
      Routine(id = "c", title = "C", isPinned = true, createdAt = now, updatedAt = now),
    )

  private fun types(
    isPro: Boolean = true,
    google: Boolean = true,
    routineEnabled: Boolean = true,
    max: Int = 10,
  ) = builder.build(isPro, google, routineEnabled, routines, max).map { it.type }

  @Test
  fun `orders shortcuts with google tasks last`() {
    val result = types()

    assertEquals(
      listOf(
        Shortcut.Reminder, Shortcut.Note, Shortcut.QuickAdd, Shortcut.StartFocus,
        Shortcut.Routine, Shortcut.Routine, Shortcut.GoogleTask,
      ),
      result,
    )
  }

  @Test
  fun `omits focus shortcut when not pro`() {
    assertEquals(false, types(isPro = false).contains(Shortcut.StartFocus))
  }

  @Test
  fun `omits routine shortcuts when routines disabled`() {
    assertEquals(false, types(routineEnabled = false).contains(Shortcut.Routine))
  }

  @Test
  fun `includes only pinned routines`() {
    val ids = builder.build(true, false, true, routines, 10).mapNotNull { it.routineId }

    assertEquals(listOf("a", "c"), ids)
  }

  @Test
  fun `omits google tasks when disabled`() {
    assertEquals(false, types(google = false).contains(Shortcut.GoogleTask))
  }

  @Test
  fun `truncates to max count keeping order`() {
    assertEquals(listOf(Shortcut.Reminder, Shortcut.Note, Shortcut.QuickAdd), types(max = 3))
  }
}
