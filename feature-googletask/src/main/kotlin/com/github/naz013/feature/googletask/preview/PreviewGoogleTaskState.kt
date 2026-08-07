package com.github.naz013.feature.googletask.preview

internal data class PreviewGoogleTaskState(
  val task: GoogleTaskPreviewState? = null,
  val isLoading: Boolean = false,
  val showDeleteConfirm: Boolean = false,
)

internal data class GoogleTaskPreviewState(
  val id: String,
  val text: String,
  val notes: String?,
  val dueDate: String?,
  val createdDate: String?,
  val completedDate: String?,
  val isCompleted: Boolean,
  val taskListName: String,
  val taskListColor: Int,
)
