package com.elementary.tasks.calendar.dayview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.home.eventsview.EventMenuAction
import com.elementary.tasks.home.eventsview.UiEventBirthday
import com.elementary.tasks.home.eventsview.UiEventItem
import com.elementary.tasks.home.eventsview.UiEventReminder
import com.elementary.tasks.reminder.scheduling.usecase.SkipReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.datetime.DateTimeManager
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

  suspend fun loadDayEvents(date: LocalDate): List<UiEventItem> =
    withContext(dispatcherProvider.default()) { getDayEventItemsUseCase(date) }

  private suspend fun applyDate(date: LocalDate) {
    val days = weekHeaderController.calculateWeek(date)
    val title = dateTimeManager.formatCalendarDate(date).capitalizeFirstLetter()
    state.update { it.copy(title = title, days = days, selectedDate = date) }
  }

  fun onItemClick(item: UiEventItem) {
    when (item) {
      is UiEventReminder -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      is UiEventBirthday -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      else -> Unit
    }
  }

  fun onEventMenuAction(
    item: UiEventItem,
    action: EventMenuAction,
  ) {
    when (item) {
      is UiEventReminder -> onReminderMenuAction(item, action)
      is UiEventBirthday -> onBirthdayMenuAction(item, action)
      else -> Unit
    }
  }

  private fun onReminderMenuAction(
    item: UiEventReminder,
    action: EventMenuAction,
  ) {
    when (action) {
      EventMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      EventMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenReminderEdit(item.id))
      EventMenuAction.ARCHIVE -> navigationEvent.value = Event(NavigationEvent.ConfirmArchiveReminder(item.id))
      EventMenuAction.SKIP -> Unit
      EventMenuAction.TURN_OFF -> onToggleReminder(item)
      EventMenuAction.DELETE -> Unit
    }
  }

  private fun onBirthdayMenuAction(
    item: UiEventBirthday,
    action: EventMenuAction,
  ) {
    when (action) {
      EventMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      EventMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayEdit(item.id))
      EventMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteBirthday(item.id))
      EventMenuAction.ARCHIVE, EventMenuAction.SKIP, EventMenuAction.TURN_OFF -> Unit
    }
  }

  private fun onToggleReminder(item: UiEventReminder) {
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
