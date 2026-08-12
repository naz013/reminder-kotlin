package com.github.naz013.ui.googletask

import androidx.annotation.ColorInt

data class GoogleTaskItemState(
  val id: String,
  val text: String,
  val notes: String?,
  val dueDate: String?,
  val isCompleted: Boolean,
  @ColorInt val taskListColor: Int?,
  val reminderId: String?,
)
