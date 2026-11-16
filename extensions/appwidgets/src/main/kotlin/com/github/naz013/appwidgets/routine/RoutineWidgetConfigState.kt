package com.github.naz013.appwidgets.routine

internal data class UiRoutineWidgetOption(
  val id: String,
  val name: String,
  val iconRes: Int?,
  val color: Int
)

internal data class RoutineWidgetConfigState(
  val routines: List<UiRoutineWidgetOption> = emptyList(),
  /** Null means "next scheduled routine". */
  val selectedRoutineId: String? = null
)

internal sealed interface RoutineWidgetConfigEvent {
  data object Saved : RoutineWidgetConfigEvent
}
