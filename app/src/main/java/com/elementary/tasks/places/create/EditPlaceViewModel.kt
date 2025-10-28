package com.elementary.tasks.places.create

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
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
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
  private val savePlaceUseCase: SavePlaceUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _place = mutableLiveDataOf<UiPlaceEdit>()
  val place = _place.toLiveData()

  var lat: Double = 0.0
  var lng: Double = 0.0
  var address: String = ""
  var markerStyle: Int = prefs.markerStyle
  var markerRadius: Int = prefs.radius

  var canDelete: Boolean = false
    private set
  var hasSameInDb: Boolean = false
    private set
  var isFromFile: Boolean = false
    private set
  private var isEdited: Boolean = false

  init {
    load()
  }

  fun hasId(): Boolean {
    return id.isNotEmpty()
  }

  fun hasLatLng(): Boolean {
    return lat != 0.0 && lng != 0.0
  }

  fun getPlace(): UiPlaceEdit? {
    return place.value
  }

  fun savePlace(data: SavePlaceData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = (placeRepository.getById(id) ?: Place(syncState = SyncState.WaitingForUpload)).apply {
        this.name = data.name
        this.dateTime = dateTimeManager.getNowGmtDateTime()
        this.radius = markerRadius
        this.latitude = lat
        this.longitude = lng
        this.marker = markerStyle
        this.syncState = SyncState.WaitingForUpload
      }
      if (data.newId) {
        place.id = UUID.randomUUID().toString()
      }
      savePlaceUseCase(place)
      Logger.logEvent("Place saved")
      postInProgress(false)
      postCommand(Commands.SAVED)
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

  fun deletePlace() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deletePlaceUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      if (place != null) {
        canDelete = true
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

    withContext(dispatcherProvider.default()) {
      _place.postValue(uiPlaceEditAdapter.convert(place))
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      hasSameInDb = place != null
    }
  }

  data class SavePlaceData(
    val name: String,
    val newId: Boolean
  )
}
