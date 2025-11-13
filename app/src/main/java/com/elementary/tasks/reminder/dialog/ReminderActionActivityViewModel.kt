package com.elementary.tasks.reminder.dialog

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.scheduling.usecase.CompleteReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderActionActivityViewModel(
  private val id: String,
  private val isTest: Boolean,
  private val reminderRepository: ReminderRepository,
  dispatcherProvider: DispatcherProvider,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val notifier: Notifier,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val snoozeReminderUseCase: SnoozeReminderUseCase,
  private val jobScheduler: JobScheduler,
  private val dateTimeManager: DateTimeManager,
  private val prefs: Prefs,
  private val getReminderActionScreenStateUseCase: CreateReminderActionScreenStateUseCase,
) : BaseProgressViewModel(dispatcherProvider) {

  private val _state = mutableLiveDataOf<ReminderActionScreenState>()
  val state = _state.toLiveData()

  private val _redirectEvent = mutableLiveEventOf<Redirect>()
  val redirectEvent = _redirectEvent.toLiveData()

  private val _showToast = mutableLiveEventOf<Int>()
  val showToast = _showToast.toLiveData()

  private val _showFavoriteNotification = mutableLiveEventOf<FavoriteNotificationData>()
  val showFavoriteNotification = _showFavoriteNotification.toLiveData()

  private var currentState: ReminderActionScreenState? = null

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      Logger.i(TAG, "Loaded reminder: ${reminder.uuId}")
      val screenState = getReminderActionScreenStateUseCase(reminder)
      currentState = screenState
      withContext(dispatcherProvider.main()) {
        _state.value = screenState
      }
    }
  }

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    if (isTest) {
      Logger.d(TAG, "Test reminder finished, deleting reminder id=$id")
      viewModelScope.launch(dispatcherProvider.io()) {
        reminderRepository.delete(id)
      }
    }
  }

  /**
   * Handles action click events from the UI.
   *
   * Dispatches the appropriate action based on the ReminderAction type.
   *
   * @param action The action that was clicked
   */
  fun onActionClick(action: com.elementary.tasks.reminder.actions.ReminderAction) {
    Logger.i(TAG, "Action clicked: $action for reminder id=$id")
    when (action) {
      com.elementary.tasks.reminder.actions.ReminderAction.Open -> onActionButtonClick()
      com.elementary.tasks.reminder.actions.ReminderAction.Complete -> onOkClicked()
      com.elementary.tasks.reminder.actions.ReminderAction.Snooze -> onSnoozeClicked(10)
      com.elementary.tasks.reminder.actions.ReminderAction.SnoozeCustom -> {
        // TODO: Show custom snooze dialog
        onSnoozeClicked(30)
      }
      com.elementary.tasks.reminder.actions.ReminderAction.Edit -> editReminder()
      com.elementary.tasks.reminder.actions.ReminderAction.Dismiss -> onCancelClicked()
      com.elementary.tasks.reminder.actions.ReminderAction.MakeCall -> onActionButtonClick()
      com.elementary.tasks.reminder.actions.ReminderAction.SendSms -> onActionButtonClick()
      com.elementary.tasks.reminder.actions.ReminderAction.SendEmail -> onActionButtonClick()
      com.elementary.tasks.reminder.actions.ReminderAction.OpenApp -> onActionButtonClick()
      com.elementary.tasks.reminder.actions.ReminderAction.OpenUrl -> onActionButtonClick()
      com.elementary.tasks.reminder.actions.ReminderAction.MoveToArchive -> {
        // TODO: Implement archive action
        onOkClicked()
      }
      com.elementary.tasks.reminder.actions.ReminderAction.Delete -> {
        // TODO: Implement delete action
        onCancelClicked()
      }
    }
  }

  /**
   * Handles todo item click events from the UI.
   *
   * Toggles the completion state of the todo item.
   *
   * @param itemId The ID of the todo item that was clicked
   */
  fun onTodoItemClick(itemId: String) {
    Logger.i(TAG, "Todo item clicked: $itemId for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      // Find and toggle the task
      val updatedTasks = reminder.shoppings.map { task ->
        if (task.uuId == itemId) {
          task.copy(isChecked = !task.isChecked)
        } else {
          task
        }
      }
      // Save the reminder with updated tasks
      val updatedReminder = reminder.copy(shoppings = updatedTasks)
      saveReminder(updatedReminder)

      // Refresh the screen state
      val screenState = getReminderActionScreenStateUseCase(updatedReminder)
      currentState = screenState
      withContext(dispatcherProvider.main()) {
        _state.value = screenState
      }
    }
  }

  fun onOkClicked() {
    Logger.i(TAG, "OK clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        _redirectEvent.value = Event(Redirect.Finish)
      }
    }
  }

  fun onFavoriteClicked() {
    Logger.i(TAG, "Favorite clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        _showFavoriteNotification.value = Event(
          FavoriteNotificationData(
            notificationId = reminder.uniqueId,
            id = reminder.uuId,
            text = reminder.summary
          )
        )
        _redirectEvent.value = Event(Redirect.Finish)
      }
    }
  }

  fun onCancelClicked() {
    Logger.i(TAG, "Cancel clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      deactivateReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        _redirectEvent.value = Event(Redirect.Finish)
      }
    }
  }

  fun onSnoozeClicked(timeInMinutes: Int) {
    Logger.i(TAG, "Snooze clicked for reminder id=$id for $timeInMinutes minutes")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      snoozeReminderUseCase(reminder, timeInMinutes)
      withContext(dispatcherProvider.main()) {
        _showToast.value = Event(R.string.reminder_snoozed)
        _redirectEvent.value = Event(Redirect.Finish)
      }
    }
  }

  fun onActionButtonClick() {
    Logger.i(TAG, "Action button clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        when {
          reminder.readType().hasSmsAction() -> {
            if (reminder.summary.isEmpty()) {
              Logger.w(TAG, "SMS message is empty, finishing.")
              _redirectEvent.value = Event(Redirect.Finish)
            } else {
              Logger.i(TAG, "Sending SMS for reminder id=${reminder.uuId}")
              _redirectEvent.value = Event(
                Redirect.SendSms(
                  target = reminder.to,
                  message = reminder.summary
                )
              )
            }
          }
          isAppType(reminder) -> {
            if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
              Logger.i(TAG, "Opening app for reminder id=${reminder.uuId}")
              _redirectEvent.value = Event(Redirect.OpenApp(reminder.target))
            } else {
              Logger.i(TAG, "Opening link for reminder id=${reminder.uuId}")
              _redirectEvent.value = Event(Redirect.OpenLink(reminder.target))
            }
          }
          Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> {
            Logger.i(TAG, "Sending email for reminder id=${reminder.uuId}")
            _redirectEvent.value = Event(
              Redirect.SendEmail(
                email = reminder.to,
                subject = reminder.subject,
                message = reminder.summary,
                filePath = reminder.attachmentFile
              )
            )
          }
          else -> {
            if (TelephonyUtil.isPhoneNumber(reminder.target)) {
              Logger.i(TAG, "Making call for reminder id=${reminder.uuId}")
              _redirectEvent.value = Event(Redirect.MakeCall(reminder.target))
            } else {
              Logger.w(TAG, "Unknown action, finishing reminder id=${reminder.uuId}")
              _redirectEvent.value = Event(Redirect.Finish)
            }
          }
        }
      }
    }
  }

  fun editReminder() {
    Logger.i(TAG, "Edit clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      deactivateReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        _redirectEvent.value = Event(Redirect.Edit(reminder.uuId))
      }
    }
  }

  fun startAgain() {
    Logger.i(TAG, "Start again clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        notifier.cancel(reminder.uniqueId)
        _redirectEvent.value = Event(Redirect.Finish)
      }
    }
  }

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      saveReminderUseCase(
        reminder.copy(
          version = reminder.version + 1,
          syncState = SyncState.WaitingForUpload
        )
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  private fun isAppType(reminder: Reminder): Boolean {
    return Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) ||
      Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)
  }

  sealed class Redirect {
    data object Finish : Redirect()
    data class Edit(val id: String): Redirect()
    data class OpenApp(val target: String): Redirect()
    data class OpenLink(val target: String): Redirect()
    data class MakeCall(val target: String): Redirect()
    data class SendSms(val target: String, val message: String): Redirect()
    data class SendEmail(
      val email: String,
      val subject: String,
      val message: String,
      val filePath: String?
    ): Redirect()
  }

  data class FavoriteNotificationData(
    val notificationId: Int,
    val id: String,
    val text: String
  )

  companion object {
    private const val TAG = "ReminderViewModel"
  }
}
