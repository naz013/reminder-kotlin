package com.elementary.tasks.places.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.places.work.PlaceDeleteBackupWorker
import com.github.naz013.domain.Place
import com.github.naz013.repository.PlaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class PlacesViewModel(
  private val backupTool: BackupTool,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val placeRepository: PlaceRepository,
  private val uiPlaceListAdapter: UiPlaceListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val placesData = SearchableData(dispatcherProvider, viewModelScope, placeRepository)
  val places = placesData.map { list ->
    list.map { uiPlaceListAdapter.convert(it) }
  }
  val shareFile = mutableLiveDataOf<ShareFile<UiPlaceList>>()

  fun onSearchUpdate(query: String) {
    placesData.onNewQuery(query)
  }

  fun deletePlace(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
      if (place == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      placeRepository.delete(place.id)
      workerLauncher.startWork(PlaceDeleteBackupWorker::class.java, Constants.INTENT_ID, place.id)
      placesData.refresh()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun sharePlace(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val place = placeRepository.getById(id)
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

  internal class SearchableData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val placeRepository: PlaceRepository
  ) : SearchableLiveData<List<Place>>(parentScope + dispatcherProvider.default()) {

    override suspend fun runQuery(query: String): List<Place> {
      return if (query.isEmpty()) {
        placeRepository.getAll()
      } else {
        placeRepository.searchByName(query.lowercase())
      }
    }
  }
}
