package com.elementary.tasks.calendar.dayview.day

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.calendar.data.DayLiveData
import com.elementary.tasks.calendar.data.EventModel
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.elementary.tasks.reminder.usecase.ScheduleReminderUploadUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class DayViewModel(
  private val date: LocalDate,
  dispatcherProvider: DispatcherProvider,
  private val dayLiveData: DayLiveData,
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val scheduleReminderUploadUseCase: ScheduleReminderUploadUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  val events: LiveData<List<EventModel>> = dayLiveData

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    Logger.d(TAG, "On resume, restoring last selected date $date")
    dayLiveData.onDateChanged(date)
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteBirthdayUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun moveToTrash(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      moveReminderToArchiveUseCase(reminder.id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun skip(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderRepository.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        scheduleReminderUploadUseCase(fromDb.uuId)
        postInProgress(false)
        postCommand(Commands.DELETED)
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }

  companion object {
    private const val TAG = "DayViewModel"
  }
}
