package com.github.naz013.tags.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.birthday.UiBirthdayListAdapter
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.note.UiNoteListItemAdapter
import com.github.naz013.ui.reminder.UiReminderListAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TagDetailsViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val groupV2Repository: GroupV2Repository,
  private val reminderV2Repository: ReminderV2Repository,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val noteRepository: NoteRepository,
  private val uiNoteListItemAdapter: UiNoteListItemAdapter,
  private val birthdayRepository: BirthdayRepository,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskItemStateAdapter: GoogleTaskItemStateAdapter,
  private val themeProvider: ThemeProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(TagDetailsState())
  val state = _state.stateInWhileSubscribed(TagDetailsState())

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val rawSections = MutableStateFlow<List<TagDetailsSection>>(emptyList())
  private val searchQuery = MutableStateFlow("")
  private val selectedType = MutableStateFlow(TagContentType.ALL)

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      combine(
        tagRepository.observeById(id),
        tagAssignmentRepository.observeItemIdsForTag(id, TaggedItemType.REMINDER),
        tagAssignmentRepository.observeItemIdsForTag(id, TaggedItemType.NOTE),
        tagAssignmentRepository.observeItemIdsForTag(id, TaggedItemType.BIRTHDAY),
        tagAssignmentRepository.observeItemIdsForTag(id, TaggedItemType.GOOGLE_TASK),
      ) { tag, reminderIds, noteIds, birthdayIds, taskIds ->
        TagAssignmentSnapshot(tag, reminderIds.toSet(), noteIds.toSet(), birthdayIds.toSet(), taskIds.toSet())
      }.collect { snapshot -> applySnapshot(snapshot) }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        rawSections,
        searchQuery,
        selectedType,
      ) { sections, query, type -> filter(sections, query, type) }
        .collect { filtered ->
          _state.update { it.copy(sections = filtered) }
        }
    }
  }

  fun onSearchQueryChange(query: String) {
    _state.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onTypeSelected(type: TagContentType) {
    _state.update { it.copy(selectedType = type) }
    selectedType.value = type
  }

  fun onEditClick() {
    navigationEvent.emit(NavigationEvent.OpenEdit(id))
  }

  fun onDeleteClick() {
    navigationEvent.emit(NavigationEvent.ConfirmDelete)
  }

  fun onDeleteConfirmed() {
    viewModelScope.launch(dispatcherProvider.io()) {
      tagAssignmentRepository.detachAllForTag(id)
      tagRepository.delete(id)
      Logger.i(TAG, "Deleted tag, id: $id")
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Deleted)
      }
    }
  }

  fun onItemClick(item: TagDetailItem) {
    val event = when (item) {
      is TagDetailItem.ReminderItem -> NavigationEvent.OpenReminderPreview(item.id)
      is TagDetailItem.NoteItem -> NavigationEvent.OpenNotePreview(item.id)
      is TagDetailItem.BirthdayItem -> NavigationEvent.OpenBirthdayPreview(item.id)
      is TagDetailItem.GoogleTaskItem -> NavigationEvent.OpenGoogleTaskPreview(item.id)
    }
    navigationEvent.emit(event)
  }

  // Driven by tagRepository.observeById/tagAssignmentRepository.observeItemIdsForTag in init - no
  // manual reload needed, this re-runs whenever the tag itself or any item's assignment to it
  // changes. Reminders/notes/birthdays/tasks are still joined fresh on each such trigger rather
  // than fully reactively - GoogleTaskRepository/GoogleTaskListRepository have no Flow support
  // yet, and notes would need archive-inclusion behavior a Flow-based query doesn't offer today.
  private suspend fun applySnapshot(snapshot: TagAssignmentSnapshot) {
    val tag = snapshot.tag ?: run {
      Logger.w(TAG, "Tag not found, id: $id")
      return
    }
    val color = themeProvider.themedColor(tag.color)

    val groupsById = groupV2Repository.getAll().associateBy { it.uuId }
    val reminders =
      reminderV2Repository.getAll(active = true, removed = false)
        .filter { it.uuId in snapshot.reminderIds }
        .map { TagDetailItem.ReminderItem(uiReminderListAdapter.createV2(it, groupsById[it.groupId])) }

    val notes =
      noteRepository.getByIds(snapshot.noteIds.toList())
        .map { TagDetailItem.NoteItem(uiNoteListItemAdapter.convert(it)) }

    val birthdays =
      birthdayRepository.getAll()
        .filter { it.uuId in snapshot.birthdayIds }
        .map { TagDetailItem.BirthdayItem(uiBirthdayListAdapter.convert(it)) }

    val listsById = googleTaskListRepository.getAll().associateBy { it.listId }
    val tasks =
      googleTaskRepository.getAll()
        .filter { it.taskId in snapshot.taskIds }
        .map { TagDetailItem.GoogleTaskItem(googleTaskItemStateAdapter.convert(it, listsById[it.listId])) }

    rawSections.value =
      listOf(
        TagDetailsSection(TagContentType.REMINDER, reminders),
        TagDetailsSection(TagContentType.NOTE, notes),
        TagDetailsSection(TagContentType.GOOGLE_TASK, tasks),
        TagDetailsSection(TagContentType.BIRTHDAY, birthdays),
      )

    _state.update { it.copy(isLoading = false, title = tag.name, color = color) }
  }

  private data class TagAssignmentSnapshot(
    val tag: Tag?,
    val reminderIds: Set<String>,
    val noteIds: Set<String>,
    val birthdayIds: Set<String>,
    val taskIds: Set<String>,
  )

  private fun filter(
    sections: List<TagDetailsSection>,
    query: String,
    type: TagContentType,
  ): List<TagDetailsSection> {
    val normalizedQuery = query.trim().lowercase()
    return sections
      .filter { type == TagContentType.ALL || it.type == type }
      .map { section ->
        if (normalizedQuery.isEmpty()) {
          section
        } else {
          section.copy(items = section.items.filter { it.searchText.lowercase().contains(normalizedQuery) })
        }
      }
      .filter { it.items.isNotEmpty() }
  }

  sealed interface NavigationEvent {
    data class OpenEdit(val id: String) : NavigationEvent

    data object ConfirmDelete : NavigationEvent

    data object Deleted : NavigationEvent

    data class OpenReminderPreview(val id: String) : NavigationEvent

    data class OpenNotePreview(val id: String) : NavigationEvent

    data class OpenBirthdayPreview(val id: String) : NavigationEvent

    data class OpenGoogleTaskPreview(val id: String) : NavigationEvent
  }

  companion object {
    private const val TAG = "TagDetailsViewModel"
  }
}
