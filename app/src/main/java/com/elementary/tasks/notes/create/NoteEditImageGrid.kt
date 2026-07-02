package com.elementary.tasks.notes.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState

private const val GRID_COLUMNS = 3

/**
 * A plain (non-lazy) grid: the note's image list is always small and unpaginated, and this
 * composable is nested inside the screen's outer `Modifier.verticalScroll()` container — a lazy
 * grid there would be measured with infinite height constraints and crash.
 */
@Composable
fun NoteEditImageGrid(
  images: List<UiNoteImage>,
  onImageClick: (Int) -> Unit,
  onRemoveClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (images.isEmpty()) return
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val cellSize = maxWidth / GRID_COLUMNS
    Column {
      images.chunked(GRID_COLUMNS).forEachIndexed { rowIndex, row ->
        Row {
          row.forEachIndexed { columnIndex, image ->
            val index = rowIndex * GRID_COLUMNS + columnIndex
            NoteEditImageItem(
              image = image,
              onClick = { onImageClick(index) },
              onRemoveClick = { onRemoveClick(index) },
              modifier = Modifier
                .width(cellSize)
                .padding(2.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NoteEditImageItem(
  image: UiNoteImage,
  onClick: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(MaterialTheme.shapes.small)
      .background(MaterialTheme.colorScheme.surfaceVariant)
  ) {
    if (image.state == UiNoteImageState.LOADING) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(24.dp))
    } else {
      AsyncImage(
        model = image.filePath,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .clickable(onClick = onClick)
      )
      Surface(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(4.dp)
          .size(20.dp)
          .clickable(onClick = onRemoveClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiary
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onTertiary,
          modifier = Modifier.padding(3.dp)
        )
      }
    }
  }
}
