package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.view_models.DispatcherProvider

class PlacesViewModel(
  prefs: Prefs,
  private val backupTool: BackupTool,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  placesDao: PlacesDao
) : BasePlacesViewModel(prefs, dispatcherProvider, workManagerProvider, placesDao) {

  val places = placesDao.loadAll()
  val shareFile = mutableLiveDataOf<ShareFile<Place>>()

  fun sharePlace(place: Place) = launchDefault {
    shareFile.postValue(ShareFile(place, backupTool.placeToFile(place)))
  }
}
