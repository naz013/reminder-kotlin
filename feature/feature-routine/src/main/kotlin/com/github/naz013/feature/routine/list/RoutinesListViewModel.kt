package com.github.naz013.feature.routine.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.routine.Routine
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.logic.routine.usecase.RoutineRecurrenceResetUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.RoutineIconSet
import com.github.naz013.ui.routine.UiRoutineListItem
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class RoutinesListViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val routineRepository: RoutineRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val routineDurationCalculator: RoutineDurationCalculator,
  private val routineRecurrenceResetUseCase: RoutineRecurrenceResetUseCase,
  private val textProvider: TextProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(RoutinesListState())
  val state = _state.stateInWhileSubscribed(RoutinesListState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val selectedTagId = MutableStateFlow<String?>(null)
  private val sortOrder = MutableStateFlow(RoutineSortOrder.CREATION_DATE)

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags -> _state.update { it.copy(allTags = tags) } }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        routineRepository.observeAll(),
        searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        selectedTagId,
        sortOrder,
      ) { routines, query, tagId, order -> RoutinesQuery(routines, query, tagId, order) }
        .flatMapLatest { flow { emit(loadRoutines(it.routines, it.query, it.tagId, it.order)) } }
        .collect { result ->
          _state.update {
            it.copy(
              listState = if (result.items.isEmpty()) {
                RoutinesListDisplayState.Empty
              } else {
                RoutinesListDisplayState.Ready(result.items)
              },
              tagsByRoutineId = result.tagsByRoutineId,
            )
          }
        }
    }
  }

  private data class RoutinesQuery(
    val routines: List<Routine>,
    val query: String,
    val tagId: String?,
    val order: RoutineSortOrder,
  )

  private data class RoutinesLoadResult(
    val items: List<UiRoutineListItem>,
    val tagsByRoutineId: Map<String, List<TagChipState>>,
  )

  private suspend fun loadRoutines(
    allRoutines: List<Routine>,
    query: String,
    tagId: String?,
    order: RoutineSortOrder,
  ): RoutinesLoadResult = withContext(dispatcherProvider.io()) {
    val resetRoutines = allRoutines.map { routineRecurrenceResetUseCase(it) }

    val tagFiltered = if (tagId == null) {
      resetRoutines
    } else {
      val ids = tagAssignmentRepository.getItemIdsForTag(tagId, TaggedItemType.ROUTINE).toSet()
      resetRoutines.filter { it.id in ids }
    }

    val searchFiltered = if (query.isBlank()) {
      tagFiltered
    } else {
      val lowerQuery = query.trim().lowercase()
      tagFiltered.filter {
        it.title.lowercase().contains(lowerQuery) || it.description?.lowercase()?.contains(lowerQuery) == true
      }
    }

    val sorted = searchFiltered.sortedWith(
      compareByDescending<Routine> { it.isPinned }.then(
        when (order) {
          RoutineSortOrder.CREATION_DATE -> compareByDescending { it.createdAt }
          RoutineSortOrder.NAME -> compareBy { it.title.lowercase() }
        }
      )
    )

    // One getTagsForItem call per visible routine - TagAssignmentRepository has no batch lookup
    // and routine lists are realistically small, so N+1 here is an acceptable trade for simplicity.
    val tagsByRoutineId = sorted.associate { routine ->
      routine.id to tagAssignmentRepository.getTagsForItem(routine.id, TaggedItemType.ROUTINE)
        .map { tagChipStateAdapter(it) }
    }

    RoutinesLoadResult(sorted.map { toUiRoutineListItem(it) }, tagsByRoutineId)
  }

  private fun toUiRoutineListItem(routine: Routine): UiRoutineListItem {
    val backgroundColor = RoutineColors.ALL.getOrElse(routine.color) { DEFAULT_COLOR }
    val contentColor = if (backgroundColor.luminance() > CONTRAST_LUMINANCE_THRESHOLD) Color.Black else Color.White
    val timedSteps = routine.sortedSteps.mapNotNull { it.scheduledTime }
    return UiRoutineListItem(
      id = routine.id,
      title = routine.title,
      description = routine.description,
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      isPinned = routine.isPinned,
      iconRes = routine.icon?.let { RoutineIconSet.ALL.getOrNull(it) },
      stepCountLabel = textProvider.getString(R.string.routine_step_count, routine.steps.size),
      durationLabel = routineDurationCalculator.formatDuration(routine.totalDurationSeconds),
      scheduleRangeLabel = if (timedSteps.isEmpty()) null else "${timedSteps.first()} - ${timedSteps.last()}",
    )
  }

  fun onSearchQueryChange(query: String) {
    _state.update { it.copy(query = query) }
    searchQuery.value = query
  }

  fun onTagSelected(tagId: String?) {
    val newTagId = if (tagId != null && tagId == selectedTagId.value) null else tagId
    _state.update { it.copy(selectedTagId = newTagId) }
    selectedTagId.value = newTagId
  }

  fun onSortOrderSelected(order: RoutineSortOrder) {
    _state.update { it.copy(sortOrder = order) }
    sortOrder.value = order
  }

  fun onAddClick() {
    navigationEvent.emit(NavigationEvent.OpenEdit(null))
  }

  fun onRoutineClick(id: String) {
    navigationEvent.emit(NavigationEvent.OpenPreview(id))
  }

  fun onStartClick(id: String) {
    navigationEvent.emit(NavigationEvent.OpenExecute(id))
  }

  sealed interface NavigationEvent {
    data class OpenEdit(
      val id: String?
    ) : NavigationEvent

    data class OpenPreview(
      val id: String
    ) : NavigationEvent

    data class OpenExecute(
      val id: String
    ) : NavigationEvent
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MS = 150L
    private const val CONTRAST_LUMINANCE_THRESHOLD = 0.5f
    private val DEFAULT_COLOR = Color(0xFF86E3CE)
  }
}
