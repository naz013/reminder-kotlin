package com.elementary.tasks.core.data.ui.google

import androidx.annotation.ColorInt

data class UiGoogleTaskList(
  val id: String,
  val text: String,
  val notes: String?,
  val dueDate: String?,
  val isCompleted: Boolean,
  @ColorInt val taskListColor: Int?,
  val reminderId: String?,
)
