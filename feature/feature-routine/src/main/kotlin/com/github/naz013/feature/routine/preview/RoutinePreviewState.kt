package com.github.naz013.feature.routine.preview

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.tag.TagChipState

internal data class RoutinePreviewStepUiState(
  val id: String,
  val title: String,
  val scheduledTime: String?,
  val durationLabel: String,
  val isCompleted: Boolean,
)

internal sealed interface RoutinePreviewState {
  data object Loading : RoutinePreviewState

  data class Ready(
    val id: String,
    val title: String,
    val description: String?,
    val backgroundColor: Color,
    val contentColor: Color,
    val isPinned: Boolean,
    val iconRes: Int?,
    val durationLabel: String,
    val stepCountLabel: String,
    val recurrenceLabel: String,
    val tags: List<TagChipState>,
    val steps: List<RoutinePreviewStepUiState>,
  ) : RoutinePreviewState
}
