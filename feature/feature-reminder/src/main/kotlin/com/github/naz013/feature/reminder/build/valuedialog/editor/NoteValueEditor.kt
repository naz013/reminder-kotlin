package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.note.UiNoteList
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.NoteBuilderItem
import androidx.compose.ui.text.font.Typeface as ComposeTypeface

private val LIST_MAX_HEIGHT = 400.dp
private const val BODY_MAX_CHARS = 500
private const val BODY_MAX_LINES = 5

/**
 * Single-select, toggleable (tap the selected note again to clear) list of notes, each rendered
 * as its own card on the note's background color - reuses the same text/image treatment as the
 * builder list's note-preview row. Replaces `NoteController`.
 */
@Composable
internal fun NoteValueEditor(
  builderItem: NoteBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var selectedId by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue()?.id) }

  LazyColumn(
    modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(builderItem.notes, key = { it.id }) { note ->
      val selected = note.id == selectedId
      NoteSelectableRow(
        note = note,
        selected = selected,
        onClick = {
          selectedId = if (selected) null else note.id
          builderItem.modifier.update(if (selected) null else note)
          onValueChange(builderItem)
        },
      )
    }
  }
}

@Composable
private fun NoteSelectableRow(note: UiNoteList, selected: Boolean, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color(note.backgroundColor))
      .clickable(onClick = onClick)
      .padding(8.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      painter = painterResource(
        if (selected) R.drawable.ic_builder_google_task_list else R.drawable.ic_fluent_radio_button,
      ),
      contentDescription = null,
      tint = Color(note.textColor),
      modifier = Modifier
        .padding(top = 4.dp, end = 8.dp)
        .size(24.dp),
    )
    Column(modifier = Modifier.weight(1f)) {
      val bodyText = if (note.text.length > BODY_MAX_CHARS) {
        note.text.substring(0, BODY_MAX_CHARS) + "..."
      } else {
        note.text
      }
      if (bodyText.isNotEmpty()) {
        val fontFamily = remember(note.typeface) {
          note.typeface?.let { FontFamily(ComposeTypeface(it)) }
        }
        Text(
          text = bodyText,
          color = Color(note.textColor),
          fontFamily = fontFamily,
          fontSize = note.fontSize.sp,
          maxLines = BODY_MAX_LINES,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (note.images.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
          model = note.images.first().filePath,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(4.dp)),
        )
      }
    }
  }
}
