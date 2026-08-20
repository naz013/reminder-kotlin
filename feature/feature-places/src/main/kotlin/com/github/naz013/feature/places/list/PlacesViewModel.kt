package com.github.naz013.feature.places.list

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.ui.common.R
import com.github.naz013.files.BackupTool
import com.github.naz013.feature.places.usecase.DeletePlaceUseCase
import com.github.naz013.ui.map.MapStyle
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentFactory
import com.github.naz013.domain.Place
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.repository.PlaceRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class PlacesViewModel(
  private val backupTool: BackupTool,
  private val dispatcherProvider: DispatcherProvider,
  private val placeRepository: PlaceRepository,
  private val deletePlaceUseCase: DeletePlaceUseCase,
  private val mapStyle: MapStyle,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val intentFactory: IntentFactory,
) : ViewModel() {

  private val _screenState = MutableStateFlow(PlacesScreenState())
  val screenState = _screenState.stateInWhileSubscribed(PlacesScreenState())
    .onStart { refresh() }
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val refreshSignal = MutableStateFlow(0)

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        refreshSignal,
      ) { query, _ -> query }
        .flatMapLatest { query ->
          flow { emit(loadMerged(query)) }
        }.collect { applyList(it) }
    }
  }

  fun onBackClicked() {
    navigationEvent.emit(NavigationEvent.MoveBack)
  }

  fun onSearchQueryChange(query: String) {
    _screenState.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onAddClick() {
    navigationEvent.emit(NavigationEvent.OpenEditPlace(""))
  }

  fun onPlaceClick(id: String) {
    navigationEvent.emit(NavigationEvent.OpenEditPlace(id))
  }

  fun onPlaceMenuAction(
    id: String,
    action: PlaceMenuAction,
  ) {
    when (action) {
      PlaceMenuAction.EDIT -> onPlaceClick(id)
      PlaceMenuAction.SHARE -> sharePlace(id)
      PlaceMenuAction.DELETE -> navigationEvent.emit(NavigationEvent.ConfirmDelete(id))
    }
  }

  fun deletePlace(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      deletePlaceUseCase(id)
      refresh()
    }
  }

  private fun refresh() {
    refreshSignal.update { it + 1 }
  }

  private fun applyList(mergedPlaces: MergedPlaces) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val states = mergedPlaces.places.map { toPlaceState(it) }.sortedBy { it.name }

      withContext(dispatcherProvider.main()) {
        _screenState.update {
          it.copy(
            listState = if (states.isEmpty() && !mergedPlaces.hasAny) {
              ListState.Empty
            } else {
              ListState.Ready(states)
            }
          )
        }
      }
    }
  }

  private fun sharePlace(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      if (place == null) {
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(NavigationEvent.ShowToast(R.string.error_sending))
        }
        return@launch
      }
      val file = backupTool.placeToFile(place)
      if (file == null || !file.exists() || !file.canRead()) {
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(NavigationEvent.ShowToast(R.string.error_sending))
        }
        return@launch
      }
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(
          NavigationEvent.ShareFile(
            createIntent(file, place.name),
            textProvider.getString(R.string.share_send_email)
          )
        )
      }
    }
  }

  private fun createIntent(file: File, name: String): Intent {
    val intent = intentFactory.createFileUriIntent(file = file)
    intent.putExtra(Intent.EXTRA_SUBJECT, name)
    return intent
  }

  private fun toPlaceState(place: Place): PlaceState {
    return PlaceState(
      markerColor = mapStyle.getMarkerColor(place.marker),
      id = place.id,
      name = place.name,
      latLng = LatLng(place.latitude, place.longitude),
      formattedDate = dateTimeManager.getPlaceDateTimeFromGmt(place.dateTime)?.let {
        dateTimeManager.getDate(it)
      },
    )
  }

  private suspend fun loadMerged(query: String): MergedPlaces {
    val all = placeRepository.getAll()
    return MergedPlaces(
      places = all.filter {
        it.name.contains(query, ignoreCase = true) ||
          (it.name.isNotEmpty() && query.contains(it.name, ignoreCase = true)) ||
          it.address.contains(query, ignoreCase = true) ||
          (it.address.isNotEmpty() && query.contains(it.address, ignoreCase = true))
      },
      hasAny = all.isNotEmpty(),
    )
  }

  private data class MergedPlaces(
    val places: List<Place>,
    val hasAny: Boolean,
  )

  sealed interface NavigationEvent {
    data class OpenEditPlace(
      val id: String,
    ) : NavigationEvent

    data class ShareFile(
      val intent: Intent,
      val name: String,
    ) : NavigationEvent

    data class ConfirmDelete(
      val id: String,
    ) : NavigationEvent

    data object MoveBack : NavigationEvent

    data class ShowToast(
      val messageRes: Int
    ) : NavigationEvent
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MS = 300L
  }
}
