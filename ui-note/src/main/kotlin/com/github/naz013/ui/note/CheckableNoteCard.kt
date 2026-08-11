package com.github.naz013.ui.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * [NoteCard] with the single-select, tap-to-toggle radio-icon-swap selection idiom baked in as
 * [trailingContent] - centralizes what used to be two copy-pasted implementations (the reminder
 * builder's note picker and the single-note widget config screen's note picker).
 */
@Composable
fun CheckableNoteCard(
  note: UiNoteListItem,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onImageClick: (imageId: Int) -> Unit = {},
) {
  NoteCard(
    note = note,
    onClick = onClick,
    modifier = modifier,
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    onImageClick = onImageClick,
    trailingContent = {
      Icon(
        painter =
          painterResource(
            if (selected) R.drawable.ic_builder_google_task_list else R.drawable.ic_fluent_radio_button,
          ),
        contentDescription = null,
        tint = note.textColor,
        modifier = Modifier.padding(8.dp).size(24.dp),
      )
    },
  )
}
