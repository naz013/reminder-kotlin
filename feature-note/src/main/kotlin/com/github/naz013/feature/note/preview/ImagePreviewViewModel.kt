package com.github.naz013.feature.note.preview

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.ui.note.UiNoteImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ImagePreviewViewModel(
  private val initialPosition: Int,
  private val imagesSingleton: ImagesSingleton,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(ImagePreviewState())
  val state = _state.stateInWhileSubscribed(ImagePreviewState())
    .onStart { loadInternal() }

  fun onPageChanged(position: Int) {
    _state.update { it.copy(position = position) }
  }

  override fun onCleared() {
    imagesSingleton.clear()
  }

  private fun loadInternal() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val state =
        ImagePreviewState(
          images = imagesSingleton.getCurrent(),
          position = initialPosition,
          background = imagesSingleton.getColor(),
        )
      withContext(dispatcherProvider.main()) {
        _state.update { state }
      }
    }
  }
}

data class ImagePreviewState(
  val images: List<UiNoteImage> = emptyList(),
  val position: Int = 0,
  val background: Color = Color.Unspecified,
  val content: Color = Color.Unspecified,
)
