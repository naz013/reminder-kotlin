package com.github.naz013.domain.home

enum class HeaderNavigationSection(
  val isAlwaysVisible: Boolean = false,
) {
  CALENDAR(isAlwaysVisible = true),
  AGENDA(isAlwaysVisible = true),
  NOTES,
  GOOGLE_TASKS,
  GROUPS,
  ROUTINES,
  WORKFLOW,
  ;

  companion object {
    val pinned: List<HeaderNavigationSection> get() = entries.filter { it.isAlwaysVisible }
    val configurable: List<HeaderNavigationSection> get() = entries.filterNot { it.isAlwaysVisible }
  }
}
