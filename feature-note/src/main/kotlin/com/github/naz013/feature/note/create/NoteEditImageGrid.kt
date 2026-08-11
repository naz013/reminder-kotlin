package com.github.naz013.feature.note.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.UiNoteImageState
import kotlinx.coroutines.delay

private const val GRID_COLUMNS = 3
private const val IMAGE_ANIMATION_DURATION_MS = 220
private const val IMAGE_STAGGER_DELAY_MS = 30L
private const val IMAGE_MAX_STAGGER_DELAY_MS = 180L

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
  modifier: Modifier = Modifier,
) {
  if (images.isEmpty()) return
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val cellSize = maxWidth / GRID_COLUMNS
    Column {
      images.chunked(GRID_COLUMNS).forEachIndexed { rowIndex, row ->
        Row {
          row.forEachIndexed { columnIndex, image ->
            val index = rowIndex * GRID_COLUMNS + columnIndex
            key(image.id) {
              NoteEditImageItem(
                image = image,
                index = index,
                onClick = { onImageClick(index) },
                onRemoveClick = { onRemoveClick(index) },
                modifier =
                  Modifier
                    .width(cellSize)
                    .padding(2.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun NoteEditImageItem(
  image: UiNoteImage,
  index: Int,
  onClick: () -> Unit,
  onRemoveClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val visibleState = remember { MutableTransitionState(false) }
  LaunchedEffect(Unit) {
    delay((index * IMAGE_STAGGER_DELAY_MS).coerceAtMost(IMAGE_MAX_STAGGER_DELAY_MS))
    visibleState.targetState = true
  }
  AnimatedVisibility(
    modifier = modifier,
    visibleState = visibleState,
    enter =
      scaleIn(
        animationSpec =
          spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
          ),
        initialScale = 0f,
      ) + fadeIn(animationSpec = tween(IMAGE_ANIMATION_DURATION_MS)),
  ) {
    Box(
      modifier =
        Modifier
          .aspectRatio(1f)
          .clip(MaterialTheme.shapes.small)
          .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
      if (image.state == UiNoteImageState.LOADING) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(24.dp))
      } else {
        AsyncImage(
          model = image.filePath,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier =
            Modifier
              .fillMaxSize()
              .clickable(onClick = onClick),
        )
        Surface(
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(4.dp)
              .size(20.dp)
              .clickable(onClick = onRemoveClick),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.tertiary,
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(3.dp),
          )
        }
      }
    }
  }
}
