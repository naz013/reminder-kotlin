package com.github.naz013.tags.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.Tag
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TagsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val themeProvider: ThemeProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(TagsScreenState())
  private val _selectedItemId = MutableStateFlow<String?>(null)
  val state = combine(_state, _selectedItemId, TagsScreenState::withSelectedItem)
    .stateInWhileSubscribed(TagsScreenState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  fun onSelectedItemIdChanged(id: String?) {
    _selectedItemId.value = id
  }

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll().map { tags ->
        tags.map { toTagState(it) }
      }.collectLatest { tags ->
        _state.update {
          it.copy(listState = if (tags.isEmpty()) TagsListState.Empty else TagsListState.Ready(tags))
        }
      }
    }
  }

  fun onAddClick() {
    navigationEvent.value = Event(NavigationEvent.OpenEdit(null))
  }

  fun onTagClick(id: String) {
    navigationEvent.value = Event(NavigationEvent.OpenDetails(id))
  }

  fun onTagMenuAction(
    tag: TagState,
    action: TagMenuAction,
  ) {
    when (action) {
      TagMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenEdit(tag.id))
      TagMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDelete(tag.id))
    }
  }

  fun deleteTag(id: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      tagAssignmentRepository.detachAllForTag(id)
      tagRepository.delete(id)
      Logger.i(TAG, "Deleted tag, id: $id")
    }
  }

  private fun toTagState(tag: Tag): TagState {
    return TagState(
      id = tag.id,
      name = tag.name,
      color = themeProvider.themedColor(tag.color),
    )
  }

  sealed interface NavigationEvent {
    data class OpenEdit(
      val id: String?
    ) : NavigationEvent

    data class OpenDetails(
      val id: String
    ) : NavigationEvent

    data class ConfirmDelete(
      val id: String
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "TagsViewModel"
  }
}
