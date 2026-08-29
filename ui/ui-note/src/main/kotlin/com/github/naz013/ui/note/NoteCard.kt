package com.github.naz013.ui.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.naz013.ui.common.compose.AppTheme
import org.koin.compose.koinInject

private const val BODY_TEXT_MAX_LINES = 4
private const val BODY_TEXT_MAX_CHARS = 500
private const val LINE_HEIGHT_RATIO = 1.25f
private val SECONDARY_IMAGE_SIZE = 96.dp
private val CONTENT_PADDING_TOP = 12.dp
private val CONTENT_PADDING_BOTTOM = 8.dp
private val CONTENT_PADDING_HORIZONTAL = 8.dp
private val TEXT_END_PADDING = 32.dp
private val THUMBNAILS_TOP_PADDING = 4.dp

/**
 * Shared note card: formatted content (see [toAnnotatedString], with the note's own default
 * per-note font resolved through [NoteFontProvider]) + optional image thumbnails, tinted with
 * the note's own background/text color. Used by every note-rendering surface (list, preview,
 * reminder builder/preview, widget pickers) so they can't drift out of sync with each other.
 * Overflow menus and selection indicators are not built in - callers supply them via
 * [trailingContent].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
  modifier: Modifier = Modifier,
  note: UiNoteListItem,
  onClick: () -> Unit,
  border: BorderStroke? = null,
  onLongClick: (() -> Unit)? = null,
  onImageClick: (imageId: Int) -> Unit = {},
  trailingContent: @Composable BoxScope.() -> Unit = {},
) {
  val context = LocalContext.current
  val noteFontProvider = koinInject<NoteFontProvider>()
  val bodyFontFamily = remember(note.fontStyle) {
    noteFontProvider.getTypeface(context, note.fontStyle)?.let {
      FontFamily(androidx.compose.ui.text.font.Typeface(it))
    }
  }
  val annotatedText = remember(note.content, note.fontSize) {
    note.content.toAnnotatedString(baseFontSizeSp = note.fontSize.toInt(), maxChars = BODY_TEXT_MAX_CHARS) { code ->
      noteFontProvider.getTypeface(context, code)?.let { FontFamily(androidx.compose.ui.text.font.Typeface(it)) }
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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
        if (annotatedText.isNotEmpty()) {
          Text(
            text = annotatedText,
            color = note.textColor,
            fontFamily = bodyFontFamily,
            fontSize = note.fontSize.sp,
            lineHeight = note.fontSize.sp * LINE_HEIGHT_RATIO,
            maxLines = BODY_TEXT_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .fillMaxWidth()
              .padding(end = TEXT_END_PADDING),
          )
        }
        if (note.images.isNotEmpty()) {
          NoteCardImages(
            images = note.images,
            onImageClick = onImageClick,
            onLongClick = onLongClick,
          )
        }
      }
      Box(modifier = Modifier.align(Alignment.TopEnd), content = trailingContent)
    }
  }
}

@Composable
private fun NoteCardImages(
  modifier: Modifier = Modifier,
  images: List<UiNoteImage>,
  onImageClick: (imageId: Int) -> Unit,
  onLongClick: (() -> Unit)? = null,
) {
  Column(modifier = modifier) {
    AsyncImage(
      model = images.first().filePath,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .height(dimensionResource(R.dimen.image_height_list))
        .clip(MaterialTheme.shapes.small)
        .combinedClickable(
          onClick = { onImageClick(images.first().id) },
          onLongClick = onLongClick
        ),
    )
    if (images.size > 1) {
      val scrollState = rememberScrollState()
      Row(
        modifier = Modifier
          .horizontalScroll(scrollState)
          .padding(top = THUMBNAILS_TOP_PADDING),
      ) {
        images.drop(1).forEach { image ->
          AsyncImage(
            model = image.filePath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(SECONDARY_IMAGE_SIZE)
              .padding(end = 4.dp)
              .clip(MaterialTheme.shapes.small)
              .combinedClickable(
                onClick = { onImageClick(image.id) },
                onLongClick = onLongClick
              ),
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun NoteCardPreview() {
  AppTheme {
    NoteCard(
      note = UiNoteListItem(
        id = "1",
        content = com.github.naz013.domain.note.NoteDocument.fromLegacy(
          title = "Shopping list",
          summary = "Milk, eggs, bread, butter, cheese, tomatoes, coffee",
        ),
        backgroundColor = androidx.compose.ui.graphics.Color(0xFFFFF59D),
        textColor = androidx.compose.ui.graphics.Color.Black,
        fontStyle = 9,
        fontSize = 14f,
        images = emptyList(),
      ),
      onClick = {},
    )
  }
}
