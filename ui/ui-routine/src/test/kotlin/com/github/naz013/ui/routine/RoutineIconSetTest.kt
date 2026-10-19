package com.github.naz013.ui.routine

import com.github.naz013.ui.common.icon.DrawableCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineIconSetTest {

  private val originalFluentIcons = listOf(
    DrawableCatalog.Fluent.Home,
    DrawableCatalog.Fluent.Heart,
    DrawableCatalog.Fluent.Star,
    DrawableCatalog.Fluent.Sleep,
    DrawableCatalog.Fluent.DrinkCoffee,
    DrawableCatalog.Fluent.FoodCake,
    DrawableCatalog.Fluent.Watch,
    DrawableCatalog.Fluent.ClockAlarm,
    DrawableCatalog.Fluent.CalendarCheckmark,
    DrawableCatalog.Fluent.Flag,
    DrawableCatalog.Fluent.Lightbulb,
    DrawableCatalog.Fluent.Person,
    DrawableCatalog.Fluent.Group,
    DrawableCatalog.Fluent.Contacts,
    DrawableCatalog.Fluent.Chat,
    DrawableCatalog.Fluent.Note,
    DrawableCatalog.Fluent.Document,
    DrawableCatalog.Fluent.Image,
    DrawableCatalog.Fluent.Cart,
    DrawableCatalog.Fluent.Place,
    DrawableCatalog.Fluent.Map,
    DrawableCatalog.Fluent.Globe,
    DrawableCatalog.Fluent.MoviesAndTv,
    DrawableCatalog.Fluent.EmojiLaugh,
    DrawableCatalog.Fluent.Broom,
    DrawableCatalog.Fluent.Timeline,
    DrawableCatalog.Fluent.History,
    DrawableCatalog.Fluent.CalendarStar,
    DrawableCatalog.Fluent.Fingerprint,
    DrawableCatalog.Fluent.MathFormula,
    DrawableCatalog.Fluent.Snooze,
    DrawableCatalog.Fluent.Extension,
  )

  @Test
  fun `keeps the original fluent icons first and in order`() {
    val head = RoutineIconSet.ALL.take(originalFluentIcons.size)

    assertEquals(originalFluentIcons, head)
  }

  @Test
  fun `appends icons after the original fluent ones`() {
    assertTrue(RoutineIconSet.ALL.size > originalFluentIcons.size)
  }

  @Test
  fun `contains no duplicate icons`() {
    assertEquals(RoutineIconSet.ALL.size, RoutineIconSet.ALL.toSet().size)
  }

  @Test
  fun `contains only non zero resource ids`() {
    assertTrue(RoutineIconSet.ALL.none { it == 0 })
  }
}
