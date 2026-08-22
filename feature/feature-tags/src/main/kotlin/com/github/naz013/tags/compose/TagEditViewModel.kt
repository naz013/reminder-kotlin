package com.github.naz013.tags.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.Tag
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.preferences.AppPreferences
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

internal class TagEditViewModel(
  private val id: String?,
  private val dispatcherProvider: DispatcherProvider,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val appPreferences: AppPreferences,
  private val themeProvider: ThemeProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(TagEditState(id = id, canDelete = id != null))
  val state = _state.stateInWhileSubscribed(TagEditState())
    .onStart { load() }
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    _state.update {
      it.copy(
        hapticFeedbackEnabled = appPreferences.hapticsEnabled,
        sliderColors = themeProvider.colorsForSliderThemed(),
      )
    }
  }

  private fun load() {
    val tagId = id ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      val tag = tagRepository.getById(tagId) ?: run {
        Logger.w(TAG, "Tag not found, id: $tagId")
        return@launch
      }
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(name = tag.name, colorPosition = tag.color, canDelete = true) }
      }
    }
  }

  fun onNameChanged(name: String) {
    _state.update { it.copy(name = name.take(MAX_NAME_LENGTH), nameError = false) }
  }

  fun onColorSelected(position: Int) {
    _state.update { it.copy(colorPosition = position) }
  }

  fun onSaveClick() {
    val name = _state.value.name.trim()
    if (name.isEmpty()) {
      _state.update { it.copy(nameError = true) }
      return
    }
    val tag = Tag(
      id = _state.value.id ?: UUID.randomUUID().toString(),
      name = name,
      color = _state.value.colorPosition
    )
    viewModelScope.launch(dispatcherProvider.io()) {
      tagRepository.save(tag)
      Logger.i(TAG, "Saved tag, id: ${tag.id}")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  fun onDeleteClick() {
    val tagId = _state.value.id ?: return
    viewModelScope.launch(dispatcherProvider.io()) {
      tagAssignmentRepository.detachAllForTag(tagId)
      tagRepository.delete(tagId)
      Logger.i(TAG, "Deleted tag, id: $tagId")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent
  }

  companion object {
    private const val TAG = "TagEditViewModel"
    private const val MAX_NAME_LENGTH = 100
  }
}
