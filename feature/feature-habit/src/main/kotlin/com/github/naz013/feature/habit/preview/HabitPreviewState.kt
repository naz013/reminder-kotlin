package com.github.naz013.feature.habit.preview

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

internal data class UiHabitStat(
  val label: String,
  val value: String
)

internal sealed interface HabitPreviewContent {
  data object Loading : HabitPreviewContent

  data class Ready(
    val name: String,
    val description: String?,
    val frequencyLabel: String,
    val backgroundColor: Color,
    val contentColor: Color,
    @DrawableRes val iconRes: Int?,
    val todayProgressLabel: String,
    val weekProgressLabel: String?,
    val checkButtonContainerColor: Color,
    val checkButtonContentColor: Color,
    val stats: List<UiHabitStat>,
    val activityWeeks: List<UiHabitActivityWeek>,
    val activitySummary: String
  ) : HabitPreviewContent
}

internal data class HabitPreviewState(
  val content: HabitPreviewContent = HabitPreviewContent.Loading,
  val isDeleteConfirmationVisible: Boolean = false,
)
