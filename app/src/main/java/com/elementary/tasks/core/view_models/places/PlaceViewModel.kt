package com.elementary.tasks.core.view_models.places

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.places.work.PlaceSingleBackupWorker
import kotlinx.coroutines.launch

class PlaceViewModel(
  key: String,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  placesDao: PlacesDao
) : BasePlacesViewModel(dispatcherProvider, workerLauncher, placesDao) {

  var place = placesDao.loadByKey(key)
  var hasSameInDb: Boolean = false

  fun savePlace(place: Place) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      placesDao.insert(place)
      workerLauncher.startWork(PlaceSingleBackupWorker::class.java, Constants.INTENT_ID, place.id)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placesDao.getByKey(id)
      hasSameInDb = place != null
    }
  }
}
