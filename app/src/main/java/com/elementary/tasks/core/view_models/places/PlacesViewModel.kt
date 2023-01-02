package com.elementary.tasks.core.view_models.places

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.launch

class PlacesViewModel(
  private val backupTool: BackupTool,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  placesDao: PlacesDao
) : BasePlacesViewModel(dispatcherProvider, workerLauncher, placesDao) {

  val places = placesDao.loadAll()
  val shareFile = mutableLiveDataOf<ShareFile<Place>>()

  fun sharePlace(place: Place) = viewModelScope.launch(dispatcherProvider.default()) {
    shareFile.postValue(ShareFile(place, backupTool.placeToFile(place)))
  }
}
