package com.github.naz013.feature.googletask.preview

import com.github.naz013.ui.tag.TagChipState

internal data class PreviewGoogleTaskState(
  val task: GoogleTaskPreviewState? = null,
  val isLoading: Boolean = false,
  val showDeleteConfirm: Boolean = false,
  val tags: List<TagChipState> = emptyList(),
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
