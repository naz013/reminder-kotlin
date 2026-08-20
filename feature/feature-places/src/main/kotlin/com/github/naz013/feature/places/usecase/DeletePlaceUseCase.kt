package com.github.naz013.feature.places.usecase

import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.PlaceRepository

internal class DeletePlaceUseCase(
  private val placeRepository: PlaceRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(placeId: String) {
    val _ = placeRepository.getById(placeId) ?: return
    placeRepository.delete(placeId)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Places,
      id = placeId,
    )
    Logger.i(TAG, "Deleted place with id = $placeId")
  }

  companion object {
    private const val TAG = "DeletePlaceUseCase"
  }
}
