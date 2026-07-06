package com.elementary.tasks.places.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.places.usecase.DeletePlaceUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Place
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.SearchableLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.PlaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.File

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PlacesViewModel(
  private val backupTool: BackupTool,
  dispatcherProvider: DispatcherProvider,
  private val placeRepository: PlaceRepository,
  private val uiPlaceListAdapter: UiPlaceListAdapter,
  private val deletePlaceUseCase: DeletePlaceUseCase,
  private val textProvider: TextProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  /** Kept for [com.elementary.tasks.simplemap.SimpleMapFragment], which injects its own instance
   *  of this ViewModel to populate the map's "recent places" layer — unrelated to the searchable
   *  list below, and always unfiltered since that instance never calls [onSearchQueryChange]. */
  private val placesData = SearchableData(dispatcherProvider, viewModelScope, placeRepository)
  val places: LiveData<List<UiPlaceList>> =
    placesData.map { list -> list.map { uiPlaceListAdapter.convert(it) } }

  val screenState: StateFlow<PlacesScreenState> field = MutableStateFlow(PlacesScreenState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val refreshSignal = MutableStateFlow(0)

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      searchQuery
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .combine(refreshSignal) { query, _ -> query }
        .flatMapLatest { query ->
          flow {
            emit(
              if (query.isEmpty()) {
                placeRepository.getAll()
              } else {
                placeRepository.searchByName(query.lowercase())
              },
            )
          }
        }
        .collect { applyList(it) }
    }
  }

  private fun refresh() {
    refreshSignal.update { it + 1 }
  }

  private fun applyList(list: List<Place>) {
    val items = list.map { uiPlaceListAdapter.convert(it) }
    screenState.update {
      it.copy(listState = if (items.isEmpty()) ListState.Empty else ListState.Ready(items))
    }
  }

  fun onSearchQueryChange(query: String) {
    screenState.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onAddClick() {
    navigationEvent.value = Event(NavigationEvent.OpenEditPlace(""))
  }

  fun onPlaceClick(id: String) {
    navigationEvent.value = Event(NavigationEvent.OpenEditPlace(id))
  }

  fun onPlaceMenuAction(
    id: String,
    action: PlaceMenuAction,
  ) {
    when (action) {
      PlaceMenuAction.EDIT -> onPlaceClick(id)
      PlaceMenuAction.SHARE -> sharePlace(id)
      PlaceMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDelete(id))
    }
  }

  fun deletePlace(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deletePlaceUseCase(id)
      refresh()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun sharePlace(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      if (place == null) {
        postInProgress(false)
        postError(textProvider.getString(R.string.error_sending))
        return@launch
      }
      val file = backupTool.placeToFile(place)
      postInProgress(false)
      if (file == null || !file.exists() || !file.canRead()) {
        postError(textProvider.getString(R.string.error_sending))
        return@launch
      }
      navigationEvent.value = Event(NavigationEvent.ShareFile(file, place.name))
    }
  }

  internal class SearchableData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val placeRepository: PlaceRepository,
  ) : SearchableLiveData<List<Place>>(parentScope + dispatcherProvider.default()) {
    override suspend fun runQuery(query: String): List<Place> =
      if (query.isEmpty()) {
        placeRepository.getAll()
      } else {
        placeRepository.searchByName(query.lowercase())
      }
  }

  sealed interface NavigationEvent {
    data class OpenEditPlace(val id: String) : NavigationEvent

    data class ShareFile(val file: File, val name: String) : NavigationEvent

    data class ConfirmDelete(val id: String) : NavigationEvent
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MS = 300L
  }
}
