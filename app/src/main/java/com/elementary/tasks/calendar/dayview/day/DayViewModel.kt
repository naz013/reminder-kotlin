package com.elementary.tasks.calendar.dayview.day

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.calendar.data.DayLiveData
import com.elementary.tasks.calendar.data.EventModel
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class DayViewModel(
  dispatcherProvider: DispatcherProvider,
  private val dayLiveData: DayLiveData,
  private val workerLauncher: WorkerLauncher,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val eventControlFactory: EventControlFactory
) : BaseProgressViewModel(dispatcherProvider) {

  val events: LiveData<List<EventModel>> = dayLiveData

  fun onDateSelected(date: LocalDate) {
    dayLiveData.onDateChanged(date)
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdayRepository.delete(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, IntentKeys.INTENT_ID, id)
    }
  }

  fun moveToTrash(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderRepository.getById(reminder.id)
      if (fromDb != null) {
        fromDb.isRemoved = true
        eventControlFactory.getController(fromDb).disable()
        reminderRepository.save(fromDb)
        postInProgress(false)
        postCommand(Commands.DELETED)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          fromDb.uuId
        )
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }

  fun skip(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderRepository.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        postInProgress(false)
        postCommand(Commands.DELETED)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          fromDb.uuId
        )
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }
}
