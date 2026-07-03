package com.elementary.tasks.birthdays.dialog

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.actions.BirthdayAction
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
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
  private val isTest: Boolean,
  private val birthdayRepository: BirthdayRepository,
  dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val createBirthdayActionScreenStateUseCase: CreateBirthdayActionScreenStateUseCase,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
) : BaseProgressViewModel(dispatcherProvider) {
  private val _state = mutableLiveDataOf<BirthdayActionScreenState>()
  val state = _state.toLiveData()

  private val _redirectEvent = mutableLiveEventOf<Redirect>()
  val redirectEvent = _redirectEvent.toLiveData()

  private val _showToast = mutableLiveEventOf<Int>()
  val showToast = _showToast.toLiveData()

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

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    if (isTest) {
      Logger.d(TAG, "Test birthday finished, deleting birthday id=$id")
      viewModelScope.launch(dispatcherProvider.io()) {
        deleteBirthdayUseCase(id)
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
      viewModelScope.launch(dispatcherProvider.main()) {
        _redirectEvent.value = Event(Redirect.Finish)
      }
      return
    }
    updateBirthday()
    viewModelScope.launch(dispatcherProvider.main()) {
      _redirectEvent.value = Event(Redirect.MakeCall(birthday.number))
    }
  }

  private fun onSendSmsClicked() {
    Logger.i(TAG, "Send SMS clicked for birthday id=$id")
    val birthday = _birthday ?: return
    if (birthday.number.isEmpty()) {
      Logger.w(TAG, "Phone number is empty, finishing.")
      viewModelScope.launch(dispatcherProvider.main()) {
        _redirectEvent.value = Event(Redirect.Finish)
      }
      return
    }
    updateBirthday()
    viewModelScope.launch(dispatcherProvider.main()) {
      _redirectEvent.value = Event(Redirect.SendSms(birthday.number))
    }
  }

  private fun updateBirthday() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val birthday = birthdayRepository.getById(id)
      if (birthday == null) {
        withContext(dispatcherProvider.main()) {
          _redirectEvent.value = Event(Redirect.Finish)
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
        _redirectEvent.value = Event(Redirect.Finish)
      }
    }
  }

  /**
   * Sealed class representing navigation redirect events.
   */
  sealed class Redirect {
    data class MakeCall(
      val phoneNumber: String,
    ) : Redirect()

    data class SendSms(
      val phoneNumber: String,
    ) : Redirect()

    data object Finish : Redirect()
  }

  companion object {
    private const val TAG = "BirthdayActionViewModel"
  }
}
