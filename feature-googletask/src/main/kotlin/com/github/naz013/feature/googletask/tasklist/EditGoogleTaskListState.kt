package com.github.naz013.feature.googletask.tasklist

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.common.R
import java.util.UUID

data class EditGoogleTaskListState(
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val nameError: Boolean = false,
  val sliderColors: List<Color> = emptyList(),
  val colorIndex: Int = 0,
  val wasDefault: Boolean = false,
  val isDefault: Boolean = false,
  val isDefaultLocked: Boolean = false,
  val isLoading: Boolean = false,
  val canDelete: Boolean = false,
  val showDeleteConfirm: Boolean = false,
  val screenTitleRes: Int = R.string.new_tasks_list,
  val hapticFeedbackEnabled: Boolean = true,
)
