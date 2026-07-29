package com.github.naz013.feature.note.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.naz013.feature.note.UiNoteImage
import com.github.naz013.feature.note.UiNoteListItem
import com.github.naz013.ui.common.compose.AppTheme

private const val BODY_TEXT_MAX_CHARS = 500
private const val BODY_TEXT_MAX_LINES = 3
private const val TITLE_MAX_LINES = 1
private val IMAGE_HEIGHT = 160.dp
private val SECONDARY_IMAGE_SIZE = 96.dp
private val CONTENT_PADDING_TOP = 12.dp
private val CONTENT_PADDING_BOTTOM = 8.dp
private val CONTENT_PADDING_HORIZONTAL = 8.dp
private val TEXT_END_PADDING = 32.dp
private val TEXT_BLOCK_SPACING = 8.dp

/**
 * Shared note preview card: title + body + optional image thumbnails, tinted with the note's own
 * background/text color. Used by every note-picking surface (widget config screens today, other
 * note screens over time) so they can't drift out of sync with each other - `title` is always
 * rendered even when `text` (the note body) is empty, unlike earlier per-module copies of this
 * card that only showed `text` and rendered blank for title-only notes.
 *
 * Custom per-note fonts (`fontStyle`/`titleFontStyle`) aren't applied yet - callers needing that
 * today still own their own typeface lookup; this card uses the default Material typography.
 */
@Composable
fun NoteListItemCard(
  note: UiNoteListItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  border: BorderStroke? = null,
  onImageClick: (imageId: Int) -> Unit = {},
  trailingContent: @Composable BoxScope.() -> Unit = {},
) {
  val bodyText = if (note.text.length > BODY_TEXT_MAX_CHARS) {
    note.text.substring(0, BODY_TEXT_MAX_CHARS) + "..."
  } else {
    note.text
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = note.backgroundColor),
    border = border,
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(
          start = CONTENT_PADDING_HORIZONTAL,
          top = CONTENT_PADDING_TOP,
          end = CONTENT_PADDING_HORIZONTAL,
          bottom = CONTENT_PADDING_BOTTOM,
        ),
      ) {
        if (note.title.isNotEmpty()) {
          Text(
            text = note.title,
            color = note.textColor,
            fontWeight = FontWeight.Bold,
            fontSize = note.titleFontSize.sp,
            maxLines = TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(end = TEXT_END_PADDING),
          )
        }
        if (bodyText.isNotEmpty()) {
          Text(
            text = bodyText,
            color = note.textColor,
            fontSize = note.fontSize.sp,
            maxLines = BODY_TEXT_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                top = if (note.title.isNotEmpty()) TEXT_BLOCK_SPACING else 0.dp,
                end = TEXT_END_PADDING,
              ),
          )
        }
        if (note.images.isNotEmpty()) {
          NoteListItemImages(images = note.images, onImageClick = onImageClick)
        }
      }
      Box(modifier = Modifier.align(Alignment.TopEnd), content = trailingContent)
    }
  }
}

@Composable
private fun NoteListItemImages(
  images: List<UiNoteImage>,
  onImageClick: (imageId: Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    AsyncImage(
      model = images.first().filePath,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .height(IMAGE_HEIGHT)
        .clip(MaterialTheme.shapes.small)
        .clickable { onImageClick(images.first().id) },
    )
    if (images.size > 1) {
      val scrollState = rememberScrollState()
      Row(modifier = Modifier.horizontalScroll(scrollState).padding(top = 4.dp)) {
        images.drop(1).forEach { image ->
          AsyncImage(
            model = image.filePath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(SECONDARY_IMAGE_SIZE)
              .padding(end = 4.dp)
              .clip(MaterialTheme.shapes.small)
              .clickable { onImageClick(image.id) },
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun NoteListItemCardPreview() {
  AppTheme {
    NoteListItemCard(
      note = UiNoteListItem(
        id = "1",
        title = "Shopping list",
        text = "Milk, eggs, bread, butter, cheese, tomatoes, coffee",
        backgroundColor = androidx.compose.ui.graphics.Color(0xFFFFF59D),
        textColor = androidx.compose.ui.graphics.Color.Black,
        fontStyle = 9,
        fontSize = 14f,
        titleFontStyle = 2,
        titleFontSize = 20f,
        images = emptyList(),
      ),
      onClick = {},
    )
  }
}
