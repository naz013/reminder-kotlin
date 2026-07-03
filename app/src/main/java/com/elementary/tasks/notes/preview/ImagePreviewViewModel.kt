package com.elementary.tasks.notes.preview

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.github.naz013.ui.common.isColorDark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ImagePreviewViewModel(
  private val imagesSingleton: ImagesSingleton,
  initialPosition: Int,
) : ViewModel() {
  private val _state =
    MutableStateFlow(
      ImagePreviewState(
        images = imagesSingleton.getCurrent(),
        position = initialPosition,
        backgroundColor = imagesSingleton.getColor(),
      ),
    )
  val state: StateFlow<ImagePreviewState> = _state

  private var initStatusBarColor: Int = -1
  private var statusBarColorSaved: Boolean = false

  fun onPageChanged(position: Int) {
    _state.update { it.copy(position = position) }
  }

  /** Snapshot of the caller screen's status bar color, so it can be restored once this screen
   *  is popped — the caller may not repaint it itself (e.g. the plain notes list). */
  fun saveStatusBarColor(
    @ColorInt color: Int,
  ) {
    if (statusBarColorSaved) return
    initStatusBarColor = color
    statusBarColorSaved = true
  }

  @ColorInt
  fun getStatusBarColor(): Int? =
    if (statusBarColorSaved) {
      initStatusBarColor.takeIf { it != -1 }
    } else {
      null
    }

  fun colorsFor(state: ImagePreviewState): ImagePreviewColors {
    if (state.backgroundColor == NO_COLOR_OVERRIDE) {
      return ImagePreviewColors(background = null, statusBarColor = null, content = null)
    }
    val contentColor = if (state.backgroundColor.isColorDark()) PURE_WHITE else PURE_BLACK
    return ImagePreviewColors(
      background = Color(state.backgroundColor),
      statusBarColor = state.backgroundColor,
      content = Color(contentColor),
    )
  }

  override fun onCleared() {
    imagesSingleton.clear()
    super.onCleared()
  }

  companion object {
    private const val NO_COLOR_OVERRIDE = -1
    private const val PURE_WHITE = android.graphics.Color.WHITE
    private const val PURE_BLACK = android.graphics.Color.BLACK
  }
}

data class ImagePreviewState(
  val images: List<UiNoteImage> = emptyList(),
  val position: Int = 0,
  val backgroundColor: Int = -1,
)

/** Colors derived from [ImagePreviewState.backgroundColor] — null fields mean "no override,
 *  fall back to the screen's default Material theme colors". */
data class ImagePreviewColors(
  val background: Color?,
  val statusBarColor: Int?,
  val content: Color?,
)
