package com.github.naz013.feature.note.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.common.compose.AppIcons
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(
  modifier: Modifier = Modifier,
  state: ImagePreviewState,
  onBackClick: () -> Unit,
  onPageChanged: (Int) -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(state.background),
  ) {
    TopAppBar(
      title = {
        if (state.images.isNotEmpty()) {
          Text(
            text = stringResource(R.string.x_out_of_x, state.position + 1, state.images.size),
            color = state.content,
          )
        }
      },
      navigationIcon = {
        IconButton(onClick = onBackClick) {
          Icon(
            painter = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            tint = state.content,
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      modifier = Modifier.statusBarsPadding(),
    )

    if (state.images.isNotEmpty()) {
      val pagerState = rememberPagerState(initialPage = state.position) { state.images.size }

      LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
      }

      HorizontalPager(
        state = pagerState,
        key = { state.images[it].id },
        modifier = Modifier
          .weight(1f),
      ) { page ->
        ZoomableImagePage(image = state.images[page])
      }
    }
  }
}

@Composable
private fun ZoomableImagePage(image: UiNoteImage) {
  val zoomableImageState = rememberZoomableImageState(rememberZoomableState())
  Box(modifier = Modifier.fillMaxSize()) {
    if (!zoomableImageState.isImageDisplayed) {
      CircularProgressIndicator(modifier = Modifier
        .align(Alignment.Center)
        .size(32.dp))
    }
    ZoomableAsyncImage(
      model = image.filePath,
      contentDescription = null,
      state = zoomableImageState,
      modifier = Modifier.fillMaxSize(),
    )
  }
}
