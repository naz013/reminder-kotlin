package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.places.work.PlaceSingleBackupWorker

class PlaceViewModel(
  key: String,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  placesDao: PlacesDao
) : BasePlacesViewModel(prefs, dispatcherProvider, workManagerProvider, placesDao) {

  var place = placesDao.loadByKey(key)
  var hasSameInDb: Boolean = false

  fun savePlace(place: Place) {
    postInProgress(true)
    launchDefault {
      placesDao.insert(place)
      startWork(PlaceSingleBackupWorker::class.java, Constants.INTENT_ID, place.id)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun findSame(id: String) {
    launchDefault {
      val place = placesDao.getByKey(id)
      hasSameInDb = place != null
    }
  }
}
