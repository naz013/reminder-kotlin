package com.github.naz013.feature.note.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.naz013.ui.note.UiNoteImage
import kotlinx.coroutines.delay

private val IMAGE_CAROUSEL_HEIGHT = 196.dp
private val IMAGE_ITEM_WIDTH = 160.dp
private val IMAGE_ITEM_SPACING = 8.dp
private const val IMAGE_ANIMATION_DURATION_MS = 220
private const val IMAGE_STAGGER_DELAY_MS = 30L
private const val IMAGE_MAX_STAGGER_DELAY_MS = 180L

/** Horizontal, cropped-edge image carousel — Compose Material3 equivalent of the previous
 *  RecyclerView + `CarouselLayoutManager` combination. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewNoteImageCarousel(
  images: List<UiNoteImage>,
  onImageClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (images.isEmpty()) return
  val carouselState = rememberCarouselState(itemCount = { images.size })
  HorizontalUncontainedCarousel(
    state = carouselState,
    itemWidth = IMAGE_ITEM_WIDTH,
    itemSpacing = IMAGE_ITEM_SPACING,
    modifier =
      modifier
        .fillMaxWidth()
        .height(IMAGE_CAROUSEL_HEIGHT),
  ) { index ->
    val image = images[index]
    val visibleState = remember(image.id) { MutableTransitionState(false) }
    LaunchedEffect(image.id) {
      delay((index * IMAGE_STAGGER_DELAY_MS).coerceAtMost(IMAGE_MAX_STAGGER_DELAY_MS))
      visibleState.targetState = true
    }
    AnimatedVisibility(
      visibleState = visibleState,
      modifier =
        Modifier
          .height(IMAGE_CAROUSEL_HEIGHT)
          .maskClip(MaterialTheme.shapes.medium),
      enter =
        fadeIn(animationSpec = tween(IMAGE_ANIMATION_DURATION_MS)) +
          scaleIn(
            animationSpec =
              spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
              ),
            initialScale = 0.85f,
          ),
    ) {
      AsyncImage(
        model = image.filePath,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onImageClick(index) },
      )
    }
  }
}
