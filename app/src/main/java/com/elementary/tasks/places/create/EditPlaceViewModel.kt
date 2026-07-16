package com.elementary.tasks.places.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.place.UiPlaceEditAdapter
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.places.usecase.DeletePlaceUseCase
import com.elementary.tasks.places.usecase.SavePlaceUseCase
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditPlaceViewModel(
  private val id: String,
  dispatcherProvider: DispatcherProvider,
  private val placeRepository: PlaceRepository,
  private val dateTimeManager: DateTimeManager,
  private val uiPlaceEditAdapter: UiPlaceEditAdapter,
  private val prefs: Prefs,
  private val intentDataReader: IntentDataReader,
  private val deletePlaceUseCase: DeletePlaceUseCase,
  private val savePlaceUseCase: SavePlaceUseCase,
) : BaseProgressViewModel(dispatcherProvider) {

  val state: StateFlow<EditPlaceState> field = MutableStateFlow(EditPlaceState())
  val navigationEvent: LiveData<Event<EditPlaceEvent>> field = mutableLiveEventOf()

  var lat: Double = 0.0
  var lng: Double = 0.0
  var address: String = ""
  var markerStyle: Int = prefs.markerStyle
  var markerRadius: Int = prefs.radius

  var hasSameInDb: Boolean = false
    private set
  var isFromFile: Boolean = false
    private set
  private var isEdited: Boolean = false
  private var loadedPlace: UiPlaceEdit? = null

  init {
    load()
  }

  fun hasId(): Boolean = id.isNotEmpty()

  fun hasLatLng(): Boolean = lat != 0.0 && lng != 0.0

  fun onNameChange(name: String) {
    state.update { it.copy(name = name, nameError = false) }
  }

  fun onSaveClick() {
    val name = state.value.name.trim()
    if (name.isEmpty()) {
      state.update { it.copy(nameError = true) }
      return
    }
    if (!hasLatLng()) {
      navigationEvent.value = Event(EditPlaceEvent.NoLocationSelected)
      return
    }
    if (isFromFile && hasSameInDb) {
      navigationEvent.value = Event(EditPlaceEvent.AskCopySaving)
    } else {
      savePlace(name, newId = false)
    }
  }

  fun savePlace(newId: Boolean = false) {
    savePlace(state.value.name.trim(), newId)
  }

  private fun savePlace(
    name: String,
    newId: Boolean,
  ) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place =
        (placeRepository.getById(id) ?: Place(syncState = SyncState.WaitingForUpload)).apply {
          this.name = name
          this.dateTime = dateTimeManager.getNowGmtDateTime()
          this.radius = markerRadius
          this.latitude = lat
          this.longitude = lng
          this.marker = markerStyle
          this.syncState = SyncState.WaitingForUpload
        }
      if (newId) {
        place.id = UUID.randomUUID().toString()
      }
      savePlaceUseCase(place)
      Logger.logEvent("Place saved")
      postInProgress(false)
      postCommand(Commands.SAVED)
      withContext(dispatcherProvider.main()) {
        navigationEvent.value = Event(EditPlaceEvent.Saved)
      }
    }
  }

  fun loadFromIntent() {
    viewModelScope.launch(dispatcherProvider.default()) {
      intentDataReader.get(IntentKeys.INTENT_ITEM, Place::class.java)?.run {
        onPlaceLoaded(this)
        findSame(this.id)
      }
    }
  }

  fun onDeleteClick() {
    navigationEvent.value = Event(EditPlaceEvent.ConfirmDelete)
  }

  fun deletePlace() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deletePlaceUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
      navigationEvent.value = Event(EditPlaceEvent.Deleted)
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      if (place != null) {
        state.update { it.copy(canDelete = true) }
        onPlaceLoaded(place)
      }
    }
  }

  private suspend fun onPlaceLoaded(place: Place) {
    if (isEdited) return
    isEdited = true

    lat = place.latitude
    lng = place.longitude
    markerStyle = place.marker
    markerRadius = place.radius
    address = place.address

    val editMarker = EditMarker(
      latLng = LatLng(place.latitude, place.longitude),
      style = place.marker,
      radius = place.radius,
      title = place.address,
    )

    val uiPlace = uiPlaceEditAdapter.convert(place)
    loadedPlace = uiPlace

    withContext(dispatcherProvider.default()) {
      state.update {
        it.copy(
          name = uiPlace.name,
          markers = listOf(editMarker),
        )
      }
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      hasSameInDb = place != null
    }
  }
}
