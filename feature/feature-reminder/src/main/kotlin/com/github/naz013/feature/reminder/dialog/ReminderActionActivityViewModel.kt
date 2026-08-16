package com.github.naz013.feature.reminder.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.reminder.actions.ReminderAction
import com.github.naz013.logic.reminder.ReminderNotifier
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeactivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ReminderAction as DomainReminderAction
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderActionActivityViewModel(
  private val id: String,
  private val reminderV2Repository: ReminderV2Repository,
  private val dispatcherProvider: DispatcherProvider,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val snoozeReminderUseCase: SnoozeReminderUseCase,
  private val reminderPreferences: ReminderPreferences,
  private val getReminderActionScreenStateUseCase: CreateReminderActionScreenStateUseCase,
  private val reminderNotifier: ReminderNotifier,
  private val textProvider: TextProvider,
) : ViewModel() {
  private val _state = mutableLiveDataOf<ReminderActionScreenState>()
  val state = _state.toLiveData()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var currentState: ReminderActionScreenState? = null
  private var _reminder: ReminderV2? = null

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      Logger.i(TAG, "Loaded reminder: ${reminder.uuId}")
      val screenState = getReminderActionScreenStateUseCase(reminder)
      currentState = screenState
      _reminder = reminder
      withContext(dispatcherProvider.main()) {
        _state.value = screenState
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
  fun onActionClick(action: ReminderAction) {
    Logger.i(TAG, "Action clicked: $action for reminder id=$id")
    when (action) {
      ReminderAction.Complete -> onOkClicked()
      ReminderAction.Snooze -> onDefaultSnoozeClicked()
      ReminderAction.SnoozeCustom -> {
        event.emit(ViewModelEvent.ShowSnoozeDialog)
      }
      ReminderAction.Dismiss -> onCancelClicked()
      ReminderAction.MakeCall -> onActionButtonClick()
      ReminderAction.SendSms -> onActionButtonClick()
      ReminderAction.SendEmail -> onActionButtonClick()
      ReminderAction.OpenApp -> onActionButtonClick()
      ReminderAction.OpenUrl -> onActionButtonClick()
      ReminderAction.ShowNotification -> onFavoriteClicked()
      else -> {
        Logger.w(TAG, "Unknown action: $action for reminder id=$id")
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
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      // Find and toggle the task
      val updatedTasks =
        reminder.shoppingItems.map { task ->
          if (task.uuId == itemId) {
            task.copy(isChecked = !task.isChecked)
          } else {
            task
          }
        }
      // Save the reminder with updated tasks
      val updatedReminder = reminder.copy(shoppingItems = updatedTasks)
      saveReminder(updatedReminder)

      // Refresh the screen state
      val screenState = getReminderActionScreenStateUseCase(updatedReminder)
      currentState = screenState
      withContext(dispatcherProvider.main()) {
        _state.value = screenState
      }
    }
  }

  /**
   * Handles custom snooze time selection from the UI.
   *
   * Snoozes the reminder for the specified number of minutes.
   *
   * @param timeInMinutes The number of minutes to snooze the reminder
   */
  fun onCustomSnooze(timeInMinutes: Int) {
    Logger.i(TAG, "Custom snooze selected: $timeInMinutes minutes for reminder id=$id")
    onSnoozeClicked(timeInMinutes)
  }

  private fun onOkClicked() {
    Logger.i(TAG, "OK clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.Finish)
      }
    }
  }

  private fun onFavoriteClicked() {
    Logger.i(TAG, "Favorite clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        reminderNotifier.showFavoriteNotification(
          text = reminder.summary,
          notificationId = reminder.uniqueId,
        )
        event.emit(ViewModelEvent.Finish)
      }
    }
  }

  private fun onCancelClicked() {
    Logger.i(TAG, "Cancel clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      deactivateReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.Finish)
      }
    }
  }

  private fun onDefaultSnoozeClicked() {
    val reminder = _reminder ?: return
    val delayMinutes = reminder.notification.delayMinutes
    val snoozeTime = delayMinutes?.takeIf { it != 0 } ?: reminderPreferences.snoozeTime
    Logger.i(TAG, "Default snooze clicked for reminder id=$id for $snoozeTime minutes")
    onSnoozeClicked(snoozeTime)
  }

  private fun onSnoozeClicked(timeInMinutes: Int) {
    Logger.i(TAG, "Snooze clicked for reminder id=$id for $timeInMinutes minutes")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      snoozeReminderUseCase(reminder, timeInMinutes)
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.ShowError(textProvider.getString(R.string.reminder_snoozed)))
        event.emit(ViewModelEvent.Finish)
      }
    }
  }

  private fun onActionButtonClick() {
    Logger.i(TAG, "Action button clicked for reminder id=$id")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      completeReminderUseCase(reminder)
      withContext(dispatcherProvider.main()) {
        when (val action = reminder.action) {
          is DomainReminderAction.Sms -> {
            if (reminder.summary.isEmpty()) {
              Logger.w(TAG, "SMS message is empty, finishing.")
              event.emit(ViewModelEvent.Finish)
            } else {
              Logger.i(TAG, "Sending SMS for reminder id=${reminder.uuId}")
              event.emit(
                ViewModelEvent.SendSms(
                  target = action.target,
                  message = reminder.summary,
                )
              )
            }
          }
          is DomainReminderAction.App -> {
            Logger.i(TAG, "Opening app for reminder id=${reminder.uuId}")
            event.emit(ViewModelEvent.OpenApp(action.target))
          }
          is DomainReminderAction.Link -> {
            Logger.i(TAG, "Opening link for reminder id=${reminder.uuId}")
            event.emit(ViewModelEvent.OpenLink(action.target))
          }
          is DomainReminderAction.Email -> {
            Logger.i(TAG, "Sending email for reminder id=${reminder.uuId}")
            event.emit(
              ViewModelEvent.SendEmail(
                email = action.target,
                subject = action.subject,
                message = reminder.summary,
                filePath = reminder.attachmentFiles.firstOrNull(),
              )
            )
          }
          is DomainReminderAction.Call -> {
            Logger.i(TAG, "Making call for reminder id=${reminder.uuId}")
            event.emit(ViewModelEvent.MakeCall(action.target))
          }
          DomainReminderAction.None, DomainReminderAction.Shopping -> {
            Logger.w(TAG, "Unknown action, finishing reminder id=${reminder.uuId}")
            event.emit(ViewModelEvent.Finish)
          }
        }
      }
    }
  }

  private fun saveReminder(reminder: ReminderV2) {
    viewModelScope.launch(dispatcherProvider.default()) {
      saveReminderUseCase(
        reminder.copy(
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
        ),
      )
    }
  }

  sealed interface ViewModelEvent {
    data object Finish : ViewModelEvent

    data class Edit(
      val id: String,
    ) : ViewModelEvent

    data class OpenApp(
      val target: String,
    ) : ViewModelEvent

    data class OpenLink(
      val target: String,
    ) : ViewModelEvent

    data class MakeCall(
      val target: String,
    ) : ViewModelEvent

    data class SendSms(
      val target: String,
      val message: String,
    ) : ViewModelEvent

    data class SendEmail(
      val email: String,
      val subject: String,
      val message: String,
      val filePath: String?,
    ) : ViewModelEvent

    data class ShowError(
      val message: String,
    ) : ViewModelEvent

    data object ShowSnoozeDialog : ViewModelEvent
  }

  companion object {
    private const val TAG = "ReminderViewModel"
  }
}
