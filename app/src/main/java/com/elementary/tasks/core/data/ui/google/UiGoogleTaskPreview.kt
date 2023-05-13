package com.elementary.tasks.core.data.ui.google

data class UiGoogleTaskPreview(
  val id: String,
  val text: String,
  val notes: String?,
  val dueDate: String?,
  val createdDate: String?,
  val completedDate: String?,
  val isCompleted: Boolean,
  val taskListName: String,
  val taskListColor: Int
)
