package com.elementary.tasks.places.list

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import kotlinx.coroutines.launch

class PlacesViewModel(
  private val backupTool: BackupTool,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val placesDao: PlacesDao,
  private val uiPlaceListAdapter: UiPlaceListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  val places = Transformations.map(placesDao.loadAll()) { list ->
    list.map { uiPlaceListAdapter.convert(it) }
  }
  val shareFile = mutableLiveDataOf<ShareFile<UiPlaceList>>()

  fun deletePlace(id: String) {
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

  fun sharePlace(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placesDao.getByKey(id)
      if (place == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      shareFile.postValue(
        ShareFile(
          uiPlaceListAdapter.convert(place),
          backupTool.placeToFile(place)
        )
      )
      postInProgress(false)
    }
  }
}
