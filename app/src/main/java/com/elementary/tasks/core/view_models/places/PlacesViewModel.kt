package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf

class PlacesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  private val backupTool: BackupTool
) : BasePlacesViewModel(appDb, prefs) {

  val places = appDb.placesDao().loadAll()
  val shareFile = mutableLiveDataOf<ShareFile<Place>>()

  fun sharePlace(place: Place) = launchDefault {
    shareFile.postValue(ShareFile(place, backupTool.placeToFile(place)))
  }
}
