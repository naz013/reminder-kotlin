package com.elementary.tasks.notes.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

private const val BODY_TEXT_MAX_CHARS = 500
private const val BODY_TEXT_MAX_LINES = 3
private const val TITLE_MAX_LINES = 1
private const val SECONDARY_IMAGE_SIZE_DP = 96
private const val LINE_HEIGHT_RATIO = 1.25f
private val CARD_CONTENT_PADDING_TOP = 12.dp
private val CARD_CONTENT_PADDING_BOTTOM = 8.dp
private val CARD_CONTENT_PADDING_HORIZONTAL = 8.dp
private val TEXT_END_PADDING = 32.dp
private val TEXT_BLOCK_SPACING = 8.dp
private val THUMBNAILS_TOP_PADDING = 4.dp

@Composable
fun NoteCard(
  note: UiNoteListItem,
  isArchived: Boolean,
  onClick: () -> Unit,
  onMenuAction: (NoteMenuAction) -> Unit,
  onImageClick: (imageId: Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val bodyFontFamily =
    remember(note.fontStyle) {
      AssetsUtil.getTypeface(context, note.fontStyle)?.let {
        FontFamily(
          androidx.compose.ui.text.font
            .Typeface(it),
        )
      }
    }
  val titleFontFamily =
    remember(note.titleFontStyle) {
      AssetsUtil.getTypeface(context, note.titleFontStyle)?.let {
        FontFamily(
          androidx.compose.ui.text.font
            .Typeface(it),
        )
      }
    }
  var menuExpanded by remember { mutableStateOf(false) }

  val bodyText =
    if (note.text.length > BODY_TEXT_MAX_CHARS) {
      note.text.substring(0, BODY_TEXT_MAX_CHARS) + "..."
    } else {
      note.text
    }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = note.backgroundColor),
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier =
          Modifier.padding(
            start = CARD_CONTENT_PADDING_HORIZONTAL,
            top = CARD_CONTENT_PADDING_TOP,
            end = CARD_CONTENT_PADDING_HORIZONTAL,
            bottom = CARD_CONTENT_PADDING_BOTTOM,
          ),
      ) {
        if (note.title.isNotEmpty()) {
          Text(
            text = note.title,
            color = note.textColor,
            fontFamily = titleFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = note.titleFontSize.sp,
            lineHeight = note.titleFontSize.sp * LINE_HEIGHT_RATIO,
            maxLines = TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(end = TEXT_END_PADDING),
          )
        }
        if (bodyText.isNotEmpty()) {
          Text(
            text = bodyText,
            color = note.textColor,
            fontFamily = bodyFontFamily,
            fontSize = note.fontSize.sp,
            lineHeight = note.fontSize.sp * LINE_HEIGHT_RATIO,
            maxLines = BODY_TEXT_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(
                  top = if (note.title.isNotEmpty()) TEXT_BLOCK_SPACING else 0.dp,
                  end = TEXT_END_PADDING,
                ),
          )
        }
        if (note.images.isNotEmpty()) {
          NoteCardImages(
            images = note.images,
            onImageClick = onImageClick,
          )
        }
      }
      Box(modifier = Modifier.align(Alignment.TopEnd)) {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_more_vertical),
          iconColor = note.textColor,
          contentDescription = stringResource(R.string.more_options),
          onClick = { menuExpanded = true },
        )
        AppDropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          items = noteMenuItems(isArchived),
          onItemClick = { id -> onMenuAction(NoteMenuAction.entries[id]) },
        )
      }
    }
  }
}

@Composable
private fun NoteCardImages(
  images: List<UiNoteImage>,
  onImageClick: (imageId: Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    AsyncImage(
      model = images.first().filePath,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier
          .fillMaxWidth()
          .height(dimensionResource(R.dimen.image_height_list))
          .clip(MaterialTheme.shapes.small)
          .clickable { onImageClick(images.first().id) },
    )
    if (images.size > 1) {
      val scrollState = rememberScrollState()
      Row(
        modifier =
          Modifier
            .horizontalScroll(scrollState)
            .padding(top = THUMBNAILS_TOP_PADDING),
      ) {
        images.drop(1).forEach { image ->
          AsyncImage(
            model = image.filePath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
              Modifier
                .size(SECONDARY_IMAGE_SIZE_DP.dp)
                .padding(end = 4.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onImageClick(image.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun noteMenuItems(isArchived: Boolean): List<PopupMenuItem> {
  val actions =
    if (isArchived) {
      listOf(
        NoteMenuAction.OPEN to R.string.open,
        NoteMenuAction.EDIT to R.string.edit,
        NoteMenuAction.UNARCHIVE to R.string.notes_unarchive,
        NoteMenuAction.DELETE to R.string.delete,
      )
    } else {
      listOf(
        NoteMenuAction.OPEN to R.string.open,
        NoteMenuAction.SHARE to R.string.share,
        NoteMenuAction.SHOW_IN_STATUS_BAR to R.string.show_note_in_notifications,
        NoteMenuAction.EDIT to R.string.edit,
        NoteMenuAction.ARCHIVE to R.string.notes_move_to_archive,
        NoteMenuAction.DELETE to R.string.delete,
      )
    }
  return actions.map { (action, titleRes) ->
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = action.iconRes())
  }
}

private fun NoteMenuAction.iconRes(): Int =
  when (this) {
    NoteMenuAction.OPEN -> R.drawable.ic_fluent_open
    NoteMenuAction.EDIT -> R.drawable.ic_fluent_edit
    NoteMenuAction.SHARE -> R.drawable.ic_fluent_share
    NoteMenuAction.SHOW_IN_STATUS_BAR -> R.drawable.ic_fluent_alert
    NoteMenuAction.ARCHIVE, NoteMenuAction.UNARCHIVE -> R.drawable.ic_fluent_archive
    NoteMenuAction.DELETE -> R.drawable.ic_fluent_delete
  }

@Preview(showBackground = true)
@Composable
private fun NoteCardPreview() {
  AppTheme {
    NoteCard(
      note =
        UiNoteListItem(
          id = "1",
          title = "Shopping list",
          text = "Milk, eggs, bread, butter, cheese, tomatoes, coffee",
          backgroundColor = Color(0xFFFFF59D),
          textColor = Color.Black,
          fontStyle = 9,
          fontSize = 14f,
          titleFontStyle = 2,
          titleFontSize = 20f,
          images = emptyList(),
        ),
      isArchived = false,
      onClick = {},
      onMenuAction = {},
      onImageClick = {},
    )
  }
}
