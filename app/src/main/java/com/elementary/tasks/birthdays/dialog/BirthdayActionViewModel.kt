package com.elementary.tasks.birthdays.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.actions.BirthdayAction
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

/**
 * ViewModel for the birthday action screen.
 *
 * Manages the state and handles user actions for birthday notifications.
 */
class BirthdayActionViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val createBirthdayActionScreenStateUseCase: CreateBirthdayActionScreenStateUseCase,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
) : ViewModel() {

  private val _state = mutableLiveDataOf<BirthdayActionScreenState>()
  val state = _state.toLiveData()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var currentState: BirthdayActionScreenState? = null
  private var _birthday: Birthday? = null

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      Logger.i(TAG, "Loaded birthday: ${birthday.uuId}")
      val screenState = createBirthdayActionScreenStateUseCase(birthday)
      currentState = screenState
      _birthday = birthday
      withContext(dispatcherProvider.main()) {
        _state.value = screenState
      }
    }
  }

  /**
   * Handles action click events from the UI.
   *
   * Dispatches the appropriate action based on the BirthdayAction type.
   *
   * @param action The action that was clicked
   */
  fun onActionClick(action: BirthdayAction) {
    Logger.i(TAG, "Action clicked: $action for birthday id=$id")
    when (action) {
      BirthdayAction.Ok -> onOkClicked()
      BirthdayAction.MakeCall -> onMakeCallClicked()
      BirthdayAction.SendSms -> onSendSmsClicked()
      else -> {
        Logger.e(TAG, "Unknown action clicked: $action")
      }
    }
  }

  private fun onOkClicked() {
    Logger.i(TAG, "OK clicked for birthday id=$id")
    updateBirthday()
  }

  private fun onMakeCallClicked() {
    Logger.i(TAG, "Make call clicked for birthday id=$id")
    val birthday = _birthday ?: return
    if (birthday.number.isEmpty()) {
      Logger.w(TAG, "Phone number is empty, finishing.")
      event.emit(ViewModelEvent.Finish)
      return
    }
    updateBirthday()
    event.emit(ViewModelEvent.MakeCall(birthday.number))
  }

  private fun onSendSmsClicked() {
    Logger.i(TAG, "Send SMS clicked for birthday id=$id")
    val birthday = _birthday ?: return
    if (birthday.number.isEmpty()) {
      Logger.w(TAG, "Phone number is empty, finishing.")
      event.emit(ViewModelEvent.Finish)
      return
    }
    updateBirthday()
    event.emit(ViewModelEvent.SendSms(birthday.number))
  }

  private fun updateBirthday() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val birthday = birthdayRepository.getById(id)
      if (birthday == null) {
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.Finish)
        }
        return@launch
      }

      saveBirthdayUseCase(
        birthday.copy(
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          showedYear = LocalDate.now().year,
        ),
      )
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.Finish)
      }
    }
  }

  sealed interface ViewModelEvent {
    data object Finish : ViewModelEvent

    data class MakeCall(
      val phoneNumber: String,
    ) : ViewModelEvent

    data class SendSms(
      val phoneNumber: String,
    ) : ViewModelEvent

    data class ShowError(
      val message: String,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "BirthdayActionViewModel"
  }
}
