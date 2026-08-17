package com.github.naz013.feature.calendar.dayview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.feature.calendar.dayview.weekheader.WeekHeaderController
import com.github.naz013.ui.agenda.AgendaMenuAction
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
import com.github.naz013.logic.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.PublicHoliday
import com.github.naz013.feature.common.capitalizeFirstLetter
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

class WeekViewViewModel(
  startDateMillis: Long,
  private val dispatcherProvider: DispatcherProvider,
  private val weekHeaderController: WeekHeaderController,
  private val dateTimeManager: DateTimeManager,
  private val getDayEventItemsUseCase: GetDayEventItemsUseCase,
  private val getDayHolidayUseCase: GetDayHolidayUseCase,
  private val reminderV2Repository: ReminderV2Repository,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val toggleReminderStateUseCase: ToggleReminderStateUseCase,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
) : ViewModel() {

  private val startDate = dateTimeManager.fromMillis(startDateMillis).toLocalDate()
  val state: StateFlow<WeekViewScreenState> field = MutableStateFlow(WeekViewScreenState(selectedDate = startDate))
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()
  val refreshSignal: StateFlow<Int> field = MutableStateFlow(0)

  val initDate: LocalDate = startDate
  var lastPosition: Int = CENTER_POSITION
    private set

  init {
    refreshSignal.update { it + 1 }
    viewModelScope.launch(dispatcherProvider.default()) {
      applyDate(startDate)
    }
  }

  fun dateForPosition(position: Int): LocalDate = initDate.plusDays((position - CENTER_POSITION).toLong())

  fun positionForDate(date: LocalDate): Int {
    val daysDiff = ChronoUnit.DAYS.between(initDate, date)
    return CENTER_POSITION + daysDiff.toInt()
  }

  fun updateLastPosition(position: Int) {
    lastPosition = position
  }

  /** Header day tap: recomputes header/title and requests the pager jump to that date. */
  fun selectDate(date: LocalDate) {
    viewModelScope.launch(dispatcherProvider.default()) {
      applyDate(date)
      navigationEvent.postValue(Event(NavigationEvent.MoveToDate(date)))
    }
  }

  /** Pager swipe settled on a date: just recomputes header/title, no pager movement requested. */
  fun onDateSelected(date: LocalDate) {
    viewModelScope.launch(dispatcherProvider.default()) {
      applyDate(date)
    }
  }

  suspend fun loadDayEvents(date: LocalDate): List<UiAgendaItem> =
    withContext(dispatcherProvider.default()) { getDayEventItemsUseCase(date) }

  suspend fun loadDayHoliday(date: LocalDate): PublicHoliday? =
    withContext(dispatcherProvider.default()) { getDayHolidayUseCase(date) }

  private suspend fun applyDate(date: LocalDate) {
    val days = weekHeaderController.calculateWeek(date)
    val title = dateTimeManager.formatCalendarDate(date).capitalizeFirstLetter()
    state.update { it.copy(title = title, days = days, selectedDate = date) }
  }

  fun onItemClick(item: UiAgendaItem) {
    when (item) {
      is UiAgendaReminder -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      is UiAgendaBirthday -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      else -> Unit
    }
  }

  fun onAgendaMenuAction(
    item: UiAgendaItem,
    action: AgendaMenuAction,
  ) {
    when (item) {
      is UiAgendaReminder -> onReminderMenuAction(item, action)
      is UiAgendaBirthday -> onBirthdayMenuAction(item, action)
      else -> Unit
    }
  }

  private fun onReminderMenuAction(
    item: UiAgendaReminder,
    action: AgendaMenuAction,
  ) {
    when (action) {
      AgendaMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      AgendaMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenReminderEdit(item.id))
      AgendaMenuAction.ARCHIVE -> navigationEvent.value = Event(NavigationEvent.ConfirmArchiveReminder(item.id))
      AgendaMenuAction.SKIP -> Unit
      AgendaMenuAction.TURN_OFF -> onToggleReminder(item)
      AgendaMenuAction.DELETE -> Unit
    }
  }

  private fun onBirthdayMenuAction(
    item: UiAgendaBirthday,
    action: AgendaMenuAction,
  ) {
    when (action) {
      AgendaMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      AgendaMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayEdit(item.id))
      AgendaMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteBirthday(item.id))
      AgendaMenuAction.ARCHIVE, AgendaMenuAction.SKIP, AgendaMenuAction.TURN_OFF -> Unit
    }
  }

  private fun onToggleReminder(item: UiAgendaReminder) {
    if (item.state.isGps) {
      navigationEvent.value = Event(NavigationEvent.RequestGpsPermission(item.id))
    } else {
      toggleReminder(item.id)
    }
  }

  fun toggleReminder(id: String) {
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        reminderV2Repository.getById(id)?.let {
          toggleReminderStateUseCase(it)
        }
      }
      refreshSignal.update { it + 1 }
    }
  }

  fun moveReminderToArchive(id: String) {
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        moveReminderToArchiveUseCase(id)
      }
      refreshSignal.update { it + 1 }
    }
  }

  fun deleteBirthday(id: String) {
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        deleteBirthdayUseCase(id)
      }
      refreshSignal.update { it + 1 }
    }
  }

  fun onAddReminderClick(date: LocalDate) {
    val millis = dateTimeManager.toMillis(LocalDateTime.of(date, LocalTime.now()))
    navigationEvent.emit(NavigationEvent.OpenNewReminder(millis))
  }

  fun onAddBirthdayClick(date: LocalDate) {
    navigationEvent.emit(NavigationEvent.OpenNewBirthday(date))
  }

  sealed interface NavigationEvent {
    data class MoveToDate(
      val date: LocalDate,
    ) : NavigationEvent

    data class OpenReminderPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenReminderEdit(
      val id: String,
    ) : NavigationEvent

    data class OpenBirthdayPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenBirthdayEdit(
      val id: String,
    ) : NavigationEvent

    data class OpenNewReminder(
      val dateMillis: Long,
    ) : NavigationEvent

    data class OpenNewBirthday(
      val date: LocalDate,
    ) : NavigationEvent

    data class ConfirmArchiveReminder(
      val id: String,
    ) : NavigationEvent

    data class ConfirmDeleteBirthday(
      val id: String,
    ) : NavigationEvent

    data class RequestGpsPermission(
      val id: String,
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "WeekViewModel"
    private const val CENTER_POSITION = Int.MAX_VALUE / 2
  }
}
