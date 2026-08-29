package com.github.naz013.domain.home

enum class HeaderNavigationSection(
  val isAlwaysVisible: Boolean = false,
  val isDisabledByDefault: Boolean = false,
) {
  CALENDAR(isAlwaysVisible = true),
  AGENDA(isAlwaysVisible = true),
  NOTES,
  BIRTHDAYS(isDisabledByDefault = true),
  GOOGLE_TASKS,
  GROUPS(isDisabledByDefault = true),
  TAG(isDisabledByDefault = true),
  ROUTINES,
  WORKFLOW,
  ;

  companion object {
    val pinned: List<HeaderNavigationSection> get() = entries.filter { it.isAlwaysVisible }
    val configurable: List<HeaderNavigationSection> get() = entries.filterNot { it.isAlwaysVisible }
  }
}
