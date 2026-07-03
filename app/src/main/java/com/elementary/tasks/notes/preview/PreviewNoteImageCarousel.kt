package com.elementary.tasks.notes.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elementary.tasks.core.data.ui.note.UiNoteImage

private val IMAGE_CAROUSEL_HEIGHT = 196.dp
private val IMAGE_ITEM_WIDTH = 160.dp
private val IMAGE_ITEM_SPACING = 8.dp

/** Horizontal, cropped-edge image carousel — Compose Material3 equivalent of the previous
 *  RecyclerView + `CarouselLayoutManager` combination. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewNoteImageCarousel(
  images: List<UiNoteImage>,
  onImageClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (images.isEmpty()) return
  val carouselState = rememberCarouselState(itemCount = { images.size })
  HorizontalUncontainedCarousel(
    state = carouselState,
    itemWidth = IMAGE_ITEM_WIDTH,
    itemSpacing = IMAGE_ITEM_SPACING,
    modifier = modifier
      .fillMaxWidth()
      .height(IMAGE_CAROUSEL_HEIGHT)
  ) { index ->
    val image = images[index]
    AsyncImage(
      model = image.filePath,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .height(IMAGE_CAROUSEL_HEIGHT)
        .maskClip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .clickable { onImageClick(index) }
    )
  }
}
