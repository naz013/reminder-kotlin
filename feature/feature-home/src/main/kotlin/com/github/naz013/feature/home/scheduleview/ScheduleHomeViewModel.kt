package com.github.naz013.feature.home.scheduleview

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.feature.home.ResolvedEventAction
import com.github.naz013.feature.home.BannerState
import com.github.naz013.feature.home.HeaderNavigationItem
import com.github.naz013.feature.home.HomeEvent
import com.github.naz013.feature.home.HomeScreenState
import com.github.naz013.feature.home.ListState
import com.github.naz013.feature.home.withSelectedEvent
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class ScheduleHomeViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val getActiveEventsForTheDayUseCase: GetActiveEventsForTheDayUseCase,
  private val getTimeSectionsUseCase: GetTimeSectionsUseCase,
  private val getGreetingTextUseCase: GetGreetingTextUseCase,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val getNavigationItemsUseCase: GetNavigationItemsUseCase,
  private val homePreferences: HomePreferences,
  private val featureFlags: FeatureFlags,
  private val whatsNewManager: WhatsNewManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val legalDocumentRepository: LegalDocumentRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(HomeScreenState())
  private val _selectedEventId = MutableStateFlow<String?>(null)
  val state = combine(_state, _selectedEventId, HomeScreenState::withSelectedEvent)
    .stateInWhileSubscribed(HomeScreenState())
    .onStart { loadData() }
  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    Logger.d(TAG, "ScheduleHomeViewModel init")
  }

  override fun onCleared() {
    super.onCleared()
    Logger.d(TAG, "ScheduleHomeViewModel onCleared")
  }

  fun onEventTypeSelected(eventType: EventType) {
    Logger.i(TAG, "On event type selected: type=$eventType")
    when (eventType) {
      EventType.Reminder -> {
        event.emit(ViewModelEvent.OpenCreateReminder)
      }

      EventType.Birthday -> {
        event.emit(ViewModelEvent.OpenCreateBirthday)
      }

      EventType.GoogleTask -> {
        event.emit(ViewModelEvent.OpenCreateGoogleTask)
      }

      EventType.Note -> {
        event.emit(ViewModelEvent.OpenCreateNote)
      }

      EventType.Todo -> {
        event.emit(ViewModelEvent.OpenCreateTodo)
      }
    }
  }

  fun onPrivacyPolicyClick() {
    Logger.i(TAG, "On privacy policy click.")
    legalDocumentRepository.markSeen(LegalDocumentType.PRIVACY_POLICY)
    event.value = Event(ViewModelEvent.OpenPrivacy)
    _state.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onPrivacyAcceptClick() {
    Logger.i(TAG, "On privacy accept click")
    legalDocumentRepository.markSeen(LegalDocumentType.PRIVACY_POLICY)
    _state.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onLoginDismissClick() {
    Logger.i(TAG, "On login dismiss click")
    homePreferences.isUserLogged = true
    _state.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onLoginClick() {
    Logger.i(TAG, "On login click")
    homePreferences.isUserLogged = true
    _state.update {
      it.copy(bannerState = getBannerState())
    }
    event.emit(ViewModelEvent.OpenCloudDrives)
  }

  fun onWhatsNewDetailsClick() {
    Logger.i(TAG, "On whats new details click")
    whatsNewManager.hideWhatsNew()
    analyticsEventSender.send(ScreenUsedEvent(Screen.WHATS_NEW))
    _state.update {
      it.copy(bannerState = getBannerState())
    }
    event.emit(ViewModelEvent.OpenWhatsNew)
  }

  fun onWhatsNewDismissClick() {
    Logger.i(TAG, "On whats new dismiss click")
    whatsNewManager.hideWhatsNew()
    _state.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onSettingsClicked() {
    Logger.i(TAG, "On settings clicked")
    event.emit(ViewModelEvent.OpenSettings)
  }

  fun onEventClicked(homeEvent: HomeEvent) {
    Logger.i(TAG, "On event clicked: id=${homeEvent.id}")
    when (homeEvent.type) {
      HomeEvent.EventType.Reminder -> {
        event.emit(ViewModelEvent.OpenReminderDetails(homeEvent.id))
      }

      HomeEvent.EventType.Birthday -> {
        event.emit(ViewModelEvent.OpenBirthdayDetails(homeEvent.id))
      }
    }
  }

  fun onSelectedEventIdChanged(id: String?) {
    _selectedEventId.value = id
  }

  fun onEventActionClicked(eventAction: HomeEvent.EventAction) {
    Logger.i(
      TAG,
      "On event action clicked: type=${eventAction::class.java.simpleName}, target=${
        Logger.private(eventAction.toString())
      }",
    )
    event.emit(ViewModelEvent.EventAction(eventAction.value))
  }

  fun onHeaderNavigationItemClicked(item: HeaderNavigationItem) {
    Logger.i(TAG, "On header navigation item clicked: ${item.navigationEvent}")
    event.emit(item.navigationEvent)
  }

  fun onHeaderNavigationItemLongClicked() {
    Logger.i(TAG, "On header navigation item long clicked")
    event.emit(ViewModelEvent.OpenHeaderItemsSettings)
  }

  private fun loadData() {
    _state.update {
      it.copy(
        greeting = getGreetingTextUseCase(),
        headerNavigationItems = emptyList(),
        addMenuItems = if (googleTasksAuthManager.isAuthorized()) {
          EventType.entries
        } else {
          listOf(EventType.Reminder, EventType.Birthday, EventType.Note, EventType.Todo)
        },
        bannerState = getBannerState(),
      )
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      val items = getNavigationItemsUseCase(this, LocalDateTime.now())
      _state.update {
        it.copy(
          headerNavigationItems = items,
        )
      }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      getActiveEventsForTheDayUseCase(LocalDateTime.now())
        .map { events -> getTimeSectionsUseCase(events) }
        .collect { sections ->
          Logger.d(TAG, "Loaded ${sections.size} sections")
          _state.update {
            it.copy(
              listState = if (sections.isEmpty()) ListState.Empty else ListState.Ready(sections),
            )
          }
        }
    }
  }

  private fun getBannerState(): BannerState? {
    if (legalDocumentRepository.hasUpdate(LegalDocumentType.PRIVACY_POLICY)) {
      Logger.v(TAG, "Privacy banner is shown")
      return BannerState.Privacy
    }
    if (!homePreferences.isUserLogged && featureFlags.isEnabled(FeatureFlag.GOOGLE_DRIVE)) {
      Logger.v(TAG, "Login banner is shown")
      return BannerState.Login
    }
    if (whatsNewManager.hasChanges()) {
      Logger.v(TAG, "Whats new banner is shown")
      return BannerState.WhatsNew
    }
    return null
  }

  sealed interface ViewModelEvent {
    data class OpenReminderDetails(
      val uuid: String,
    ) : ViewModelEvent

    data class OpenBirthdayDetails(
      val uuid: String,
    ) : ViewModelEvent

    data object OpenSettings : ViewModelEvent

    data object OpenHeaderItemsSettings : ViewModelEvent

    data class ShowEventTypeSelection(
      val types: List<EventType>,
    ) : ViewModelEvent

    data object OpenCreateReminder : ViewModelEvent

    data object OpenCreateBirthday : ViewModelEvent

    data object OpenCreateGoogleTask : ViewModelEvent

    data object OpenCreateNote : ViewModelEvent

    data object OpenAgenda : ViewModelEvent

    data object OpenCalendar : ViewModelEvent

    data object OpenNotes : ViewModelEvent

    data object OpenBirthdays : ViewModelEvent

    data object OpenGoogleTasks : ViewModelEvent

    data object OpenGroups : ViewModelEvent

    data object OpenRoutines : ViewModelEvent

    data object OpenWorkflowGallery : ViewModelEvent

    data object OpenPrivacy : ViewModelEvent

    data object OpenCloudDrives : ViewModelEvent

    data object OpenWhatsNew : ViewModelEvent

    data class EventAction(
      val value: ResolvedEventAction,
    ) : ViewModelEvent

    data object OpenCreateTodo : ViewModelEvent
  }

  enum class EventType(
    @param:StringRes val title: Int,
  ) {
    Reminder(R.string.reminder),
    Birthday(R.string.birthday),
    GoogleTask(R.string.google_task),
    Note(R.string.note),
    Todo(R.string.todo)
  }

  companion object {
    private const val TAG = "ScheduleHomeViewModel"
  }
}
