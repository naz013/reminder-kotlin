package com.elementary.tasks.notes.create

import android.content.ClipData
import com.github.naz013.domain.Tag

/**
 * Bundles every user-interaction callback for the note edit screen so the composable tree
 * (screen -> top bar / bottom bar / image grid / dialogs) doesn't need a long parameter list
 * at every level.
 */
data class NoteEditActions(
  val onBackClick: () -> Unit = {},
  val onSaveClick: () -> Unit = {},
  val onShareClick: () -> Unit = {},
  val onDeleteClick: () -> Unit = {},
  val onMicClick: () -> Unit = {},
  val onColorTabClick: () -> Unit = {},
  val onImageTabClick: () -> Unit = {},
  val onImagePickFromGallery: () -> Unit = {},
  val onImagePickFromCamera: () -> Unit = {},
  val onImagePickFromUrl: () -> Unit = {},
  val onReminderTabClick: () -> Unit = {},
  val onFontTabClick: () -> Unit = {},
  val onColorSelected: (Int) -> Unit = {},
  val onOpacityChanged: (Int) -> Unit = {},
  val onReminderAttachedChanged: (Boolean) -> Unit = {},
  val onDateClick: () -> Unit = {},
  val onTimeClick: () -> Unit = {},
  val onFontSizeChanged: (Int) -> Unit = {},
  val onFieldFocused: (NoteTextField) -> Unit = {},
  val onImageOpen: (Int) -> Unit = {},
  val onImageRemove: (Int) -> Unit = {},
  val onFontStyleSelected: (Int) -> Unit = {},
  val onPaletteSelected: (Int) -> Unit = {},
  val onDeleteConfirmed: () -> Unit = {},
  val onSameNoteKeep: () -> Unit = {},
  val onSameNoteReplace: () -> Unit = {},
  val onDialogDismiss: () -> Unit = {},
  val onDrop: (ClipData) -> Unit = {},
  val onTagsTabClick: () -> Unit = {},
  val onTagToggle: (Tag) -> Unit = {},
  val onManageTagsClick: () -> Unit = {},
)
