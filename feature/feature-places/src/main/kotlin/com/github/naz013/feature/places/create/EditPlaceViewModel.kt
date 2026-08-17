package com.github.naz013.feature.places.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.ui.map.MapPreferences
import com.github.naz013.feature.places.PlacesNavKey
import com.github.naz013.feature.places.usecase.DeletePlaceUseCase
import com.github.naz013.feature.places.usecase.SavePlaceUseCase
import com.github.naz013.ui.map.MarkerState
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.ui.common.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditPlaceViewModel(
  private val key: PlacesNavKey.Edit,
  private val dispatcherProvider: DispatcherProvider,
  private val placeRepository: PlaceRepository,
  private val dateTimeManager: DateTimeManager,
  private val mapPreferences: MapPreferences,
  private val intentDataReader: IntentDataReader,
  private val deletePlaceUseCase: DeletePlaceUseCase,
  private val savePlaceUseCase: SavePlaceUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(EditPlaceState())
  val state = _state.stateInWhileSubscribed(EditPlaceState())
    .onStart { loadInitial() }
  val navigationEvent: LiveData<Event<EditPlaceEvent>> field = mutableLiveEventOf()

  fun onNameChange(name: String) {
    _state.update { it.copy(name = name, nameError = false) }
    updateSaveState()
  }

  fun onSaveClick() {
    val name = _state.value.name.trim()
    if (name.isEmpty()) {
      _state.update { it.copy(nameError = true) }
      return
    }
    if (!_state.value.hasLatLng()) {
      navigationEvent.emit(EditPlaceEvent.NoLocationSelected)
      return
    }
    if (_state.value.isFromFile && _state.value.hasSameInDb) {
      navigationEvent.emit(EditPlaceEvent.AskCopySaving)
    } else {
      savePlace(newId = false)
    }
  }

  fun savePlace(newId: Boolean = false) {
    val state = _state.value
    viewModelScope.launch(dispatcherProvider.default()) {
      val place =
        (placeRepository.getById(key.id) ?: Place(syncState = SyncState.WaitingForUpload)).apply {
          this.name = state.name
          this.dateTime = dateTimeManager.getNowGmtDateTime()
          this.radius = state.markerRadius
          this.latitude = state.lat
          this.longitude = state.lng
          this.marker = state.markerStyle
          this.syncState = SyncState.WaitingForUpload
        }
      if (newId) {
        place.id = UUID.randomUUID().toString()
      }
      savePlaceUseCase(place)
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(EditPlaceEvent.MoveBack)
      }
    }
  }

  fun onDeleteClick() {
    navigationEvent.emit(EditPlaceEvent.ConfirmDelete)
  }

  fun deletePlace() {
    viewModelScope.launch(dispatcherProvider.io()) {
      deletePlaceUseCase(key.id)

      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(EditPlaceEvent.MoveBack)
      }
    }
  }

  fun onMarkerPlaced(markerState: MarkerState) {
    _state.update {
      it.copy(
        lat = markerState.latLng.latitude,
        lng = markerState.latLng.longitude,
        address = markerState.address,
        markerStyle = markerState.styleIndex,
        markerRadius = markerState.radius,
      )
    }
    if (_state.value.name.isEmpty()) {
      onNameChange(markerState.address)
    }
    updateSaveState()
  }

  private fun loadInitial() {
    _state.update {
      it.copy(
        markerStyle = mapPreferences.markerStyle,
        markerRadius = mapPreferences.radius,
      )
    }
    if (!key.fromIntentData) {
      viewModelScope.launch(dispatcherProvider.io()) {
        val place = placeRepository.getById(key.id)
        if (place != null) {
          onPlaceLoaded(place)
        }
      }
    } else {
      loadFromIntent()
    }
  }

  private suspend fun onPlaceLoaded(place: Place) {
    val editMarker = EditMarker(
      latLng = LatLng(place.latitude, place.longitude),
      style = place.marker,
      radius = place.radius,
      title = place.address,
    )

    withContext(dispatcherProvider.main()) {
      _state.update {
        it.copy(
          screenTitle = R.string.edit_place,
          id = place.id,
          name = place.name,
          canDelete = true,
          lat = place.latitude,
          lng = place.longitude,
          markerStyle = place.marker,
          markerRadius = place.radius,
          address = place.address,
          markers = listOf(editMarker),
          canSave = true,
        )
      }
    }
  }

  private fun loadFromIntent() {
    viewModelScope.launch(dispatcherProvider.io()) {
      intentDataReader.get(IntentKeys.INTENT_ITEM, Place::class.java)?.run {
        onPlaceLoaded(this)
        findSame(this.id)
      }
    }
  }

  private fun findSame(id: String) {
    _state.update {
      it.copy(isFromFile = true)
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      val place = placeRepository.getById(id)

      if (place != null) {
        Logger.v(TAG, "Place loaded from file, has same in the Database")
        _state.update {
          it.copy(
            id = place.id,
            hasSameInDb = true,
          )
        }
      }
    }
  }

  private fun updateSaveState() {
    _state.update {
      it.copy(
        canSave = _state.value.name.isNotEmpty() && _state.value.hasLatLng()
      )
    }
  }

  sealed interface EditPlaceEvent {
    data object MoveBack : EditPlaceEvent

    data object NoLocationSelected : EditPlaceEvent

    data object ConfirmDelete : EditPlaceEvent

    data object AskCopySaving : EditPlaceEvent
  }

  companion object {
    private const val TAG = "EditPlaceViewModel"
  }
}
