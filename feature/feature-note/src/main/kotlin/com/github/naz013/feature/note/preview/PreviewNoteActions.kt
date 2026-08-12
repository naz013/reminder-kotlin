package com.github.naz013.feature.note.preview

/**
 * Bundles every user-interaction callback for the note preview screen so the composable tree
 * (screen -> top bar / image carousel / reminder row / dialogs) doesn't need a long parameter
 * list at every level.
 */
internal data class PreviewNoteActions(
  val onBackClick: () -> Unit = {},
  val onEditClick: () -> Unit = {},
  val onStatusClick: () -> Unit = {},
  val onShareClick: () -> Unit = {},
  val onArchiveClick: () -> Unit = {},
  val onDeleteClick: () -> Unit = {},
  val onImageOpen: (Int) -> Unit = {},
  val onReminderEditClick: (String) -> Unit = {},
  val onReminderDetachClick: (String) -> Unit = {},
)
