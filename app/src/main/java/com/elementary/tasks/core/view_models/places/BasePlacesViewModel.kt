package com.elementary.tasks.core.view_models.places

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import kotlinx.coroutines.launch

abstract class BasePlacesViewModel(
  dispatcherProvider: DispatcherProvider,
  protected val workerLauncher: WorkerLauncher,
  protected val placesDao: PlacesDao
) : BaseProgressViewModel(dispatcherProvider) {

  fun deletePlace(place: Place) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      placesDao.delete(place)
      workerLauncher.startWork(PlaceDeleteBackupWorker::class.java, Constants.INTENT_ID, place.id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
