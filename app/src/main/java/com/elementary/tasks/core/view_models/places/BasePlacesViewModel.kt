package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker

abstract class BasePlacesViewModel(
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  protected val placesDao: PlacesDao
) : BaseDbViewModel(prefs, dispatcherProvider, workManagerProvider) {

  fun deletePlace(place: Place) {
    postInProgress(true)
    launchDefault {
      placesDao.delete(place)
      startWork(PlaceDeleteBackupWorker::class.java, Constants.INTENT_ID, place.id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
