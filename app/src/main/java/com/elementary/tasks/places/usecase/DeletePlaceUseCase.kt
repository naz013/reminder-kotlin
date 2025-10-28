package com.elementary.tasks.places.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.sync.DataType

class DeletePlaceUseCase(
  private val placeRepository: PlaceRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(placeId: String) {
    val place = placeRepository.getById(placeId)
    if (place == null) {
      return
    }
    placeRepository.delete(placeId)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Places,
      id = placeId
    )
    Logger.i(TAG, "Deleted place with id = $placeId")
  }

  companion object {
    private const val TAG = "DeletePlaceUseCase"
  }
}
