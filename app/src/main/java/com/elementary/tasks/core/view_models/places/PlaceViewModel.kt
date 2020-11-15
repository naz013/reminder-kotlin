package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.places.work.PlaceSingleBackupWorker

class PlaceViewModel(
  key: String,
  appDb: AppDb,
  prefs: Prefs
) : BasePlacesViewModel(appDb, prefs) {

  var place = appDb.placesDao().loadByKey(key)
  var hasSameInDb: Boolean = false

  fun savePlace(place: Place) {
    postInProgress(true)
    launchDefault {
      appDb.placesDao().insert(place)
      startWork(PlaceSingleBackupWorker::class.java, Constants.INTENT_ID, place.id)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun findSame(id: String) {
    launchDefault {
      val place = appDb.placesDao().getByKey(id)
      hasSameInDb = place != null
    }
  }
}
