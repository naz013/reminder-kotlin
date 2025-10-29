package com.elementary.tasks.places.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.sync.DataType

class SavePlaceUseCase(
  private val placeRepository: PlaceRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(place: Place) {
    placeRepository.save(place)
    placeRepository.updateSyncState(place.id, SyncState.WaitingForUpload)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Places,
      id = place.id
    )
    Logger.i(TAG, "Saved place with id = ${place.id}")
  }

  companion object {
    private const val TAG = "SavePlaceUseCase"
  }
}
