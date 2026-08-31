package com.github.naz013.tags.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Tag
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.selection.clearSelection
import com.github.naz013.ui.common.selection.select
import com.github.naz013.ui.common.selection.selectedCount
import com.github.naz013.ui.common.selection.selectedIds
import com.github.naz013.ui.common.selection.toggleSelection
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TagsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val textProvider: TextProvider,
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
    if (_state.value.selectedCount > 0) {
      updateSelection { it.toggleSelection(id) }
    } else {
      navigationEvent.value = Event(NavigationEvent.OpenDetails(id))
    }
  }

  fun onTagLongClick(id: String) {
    updateSelection { it.select(id) }
  }

  fun onSelectionCancel() {
    updateSelection { it.clearSelection() }
  }

  private fun updateSelection(transform: (List<TagState>) -> List<TagState>) {
    _state.update { state ->
      val listState = state.listState
      if (listState !is TagsListState.Ready) return@update state
      val tags = transform(listState.tags)
      state.copy(listState = TagsListState.Ready(tags), selectedCount = tags.selectedCount())
    }
  }

  private fun selectedIds(): Set<String> =
    (_state.value.listState as? TagsListState.Ready)?.tags.orEmpty().selectedIds()

  fun onDeleteSelectedClick() {
    val ids = selectedIds()
    if (ids.isEmpty()) return
    navigationEvent.value = Event(
      NavigationEvent.ConfirmDeleteSelected(
        ids = ids,
        title = textProvider.getText(R.string.tags_delete_selected_permanently, ids.size),
      ),
    )
  }

  fun deleteSelectedTags(ids: Set<String>) {
    viewModelScope.launch(dispatcherProvider.io()) {
      ids.forEach { id ->
        tagAssignmentRepository.detachAllForTag(id)
        tagRepository.delete(id)
      }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }
    }
  }

  fun applySelectedColor(colorPosition: Int) {
    val ids = selectedIds()
    if (ids.isEmpty()) return
    viewModelScope.launch(dispatcherProvider.io()) {
      ids.forEach { id ->
        val tag = tagRepository.getById(id) ?: return@forEach
        tagRepository.save(tag.copy(color = colorPosition))
      }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }
    }
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

    data class ConfirmDeleteSelected(
      val ids: Set<String>,
      val title: String,
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "TagsViewModel"
  }
}
