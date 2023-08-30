package com.elementary.tasks.places.create

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.place.UiPlaceEditAdapter
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import com.elementary.tasks.places.work.PlaceSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PlaceViewModel(
  private val id: String,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val placesDao: PlacesDao,
  private val dateTimeManager: DateTimeManager,
  private val uiPlaceEditAdapter: UiPlaceEditAdapter,
  private val contextProvider: ContextProvider
) : BaseProgressViewModel(dispatcherProvider) {

  private val _place = mutableLiveDataOf<UiPlaceEdit>()
  val place = _place.toLiveData()

  var lat: Double = 0.0
  var lng: Double = 0.0
  var address: String = ""

  var canDelete: Boolean = false
    private set
  var hasSameInDb: Boolean = false
    private set
  var isFromFile: Boolean = false
    private set
  var isLogged = false
  private var isEdited: Boolean = false

  init {
    load()
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
      val place = (placesDao.getByKey(id) ?: Place()).apply {
        this.name = data.name
        this.dateTime = dateTimeManager.getNowGmtDateTime()
        this.radius = data.radius
        this.latitude = lat
        this.longitude = lng
        this.marker = data.marker
      }
      if (data.newId) {
        place.id = UUID.randomUUID().toString()
      }
      placesDao.insert(place)
      workerLauncher.startWork(PlaceSingleBackupWorker::class.java, Constants.INTENT_ID, place.id)
      Traces.logEvent("Place saved")
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun loadFromUri(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri, FileConfig.FILE_NAME_PLACE)
          if (any != null && any is Place) {
            onPlaceLoaded(any)
          }
        }
      }
    }
  }

  fun loadFromIntent(place: Place?) {
    if (place == null) return
    viewModelScope.launch(dispatcherProvider.default()) {
      onPlaceLoaded(place)
      findSame(place.id)
    }
  }

  fun deletePlace() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placesDao.getByKey(id)
      if (place == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      placesDao.delete(place)
      workerLauncher.startWork(PlaceDeleteBackupWorker::class.java, Constants.INTENT_ID, place.id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placesDao.getByKey(id)
      if (place != null) {
        canDelete = true
        onPlaceLoaded(place)
      }
    }
  }

  private suspend fun onPlaceLoaded(place: Place) {
    if (isEdited) return
    isEdited = true
    withContext(dispatcherProvider.default()) {
      _place.postValue(uiPlaceEditAdapter.convert(place))
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placesDao.getByKey(id)
      hasSameInDb = place != null
    }
  }

  data class SavePlaceData(
    val name: String,
    val marker: Int,
    val radius: Int,
    val newId: Boolean
  )
}
