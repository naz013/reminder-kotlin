package com.github.naz013.feature.reminder.build.quickstart

import com.github.naz013.ui.common.R

/**
 * The empty-state quick-start actions on [com.elementary.tasks.reminder.build.BuildReminderScreen]:
 * each one is a shortcut that pre-populates the builder list with a small, ready-to-edit set of
 * items (via [QuickStartItemsProvider]) instead of the user picking every item one by one.
 */
enum class QuickStartOption(val labelRes: Int) {
  ONE_TIME(R.string.builder_quick_start_one_time),
  EVERY_WEEKDAY(R.string.builder_quick_start_every_weekday),
  EVERY_MONTH_DAY(R.string.builder_quick_start_every_month_day),
  EVERY_YEAR(R.string.builder_quick_start_every_year),
  COUNTDOWN_TIMER(R.string.builder_quick_start_timer),
  SHOPPING_LIST(R.string.builder_quick_start_shopping_list),
  LEAVING_PLACE(R.string.builder_quick_start_leaving_place),
  LEAVING_PLACE_DELAYED(R.string.builder_quick_start_leaving_place_delayed),
}
