package com.github.naz013.ui.routine

import com.github.naz013.ui.common.icon.DrawableCatalog

/**
 * 32 icons from [DrawableCatalog.Fluent] a routine can pick as its identity icon.
 * [com.github.naz013.domain.routine.Routine.icon] is an index into this list (null = no icon
 * selected). The order is fixed and only ever appended to - existing saved routines depend on
 * index stability, so never reorder or remove an entry.
 */
object RoutineIconSet {
  val ALL = listOf(
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
}
