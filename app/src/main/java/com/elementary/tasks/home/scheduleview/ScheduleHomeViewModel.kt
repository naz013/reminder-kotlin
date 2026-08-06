package com.elementary.tasks.home.scheduleview

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.eventaction.ResolvedEventAction
import com.elementary.tasks.home.BannerState
import com.elementary.tasks.home.HeaderNavigationItem
import com.elementary.tasks.home.HomeEvent
import com.elementary.tasks.home.HomeScreenState
import com.elementary.tasks.home.ListState
import com.elementary.tasks.whatsnew.WhatsNewManager
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
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
  private val prefs: Prefs,
  private val featureManager: FeatureManager,
  private val whatsNewManager: WhatsNewManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val legalDocumentRepository: LegalDocumentRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(HomeScreenState())
  val state = _state.stateInWhileSubscribed(HomeScreenState())
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
    prefs.isUserLogged = true
    _state.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onLoginClick() {
    Logger.i(TAG, "On login click")
    prefs.isUserLogged = true
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

  private fun loadData() {
    _state.update {
      it.copy(
        greeting = getGreetingTextUseCase(),
        headerNavigationItems = emptyList(),
        addMenuItems =
          if (googleTasksAuthManager.isAuthorized()) {
            EventType.entries
          } else {
            listOf(EventType.Reminder, EventType.Birthday, EventType.Note)
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
      val now = LocalDateTime.now()
      val events = getActiveEventsForTheDayUseCase(this, now)
      val sections = getTimeSectionsUseCase(events)
      Logger.d(TAG, "Loaded ${sections.size} sections")
      _state.update {
        it.copy(
          listState = if (sections.isEmpty()) ListState.Empty else ListState.Ready(sections),
        )
      }
    }
  }

  private fun getBannerState(): BannerState? {
    if (legalDocumentRepository.hasUpdate(LegalDocumentType.PRIVACY_POLICY)) {
      Logger.v(TAG, "Privacy banner is shown")
      return BannerState.Privacy
    }
    if (!prefs.isUserLogged && featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)) {
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

    data object OpenGoogleTasks : ViewModelEvent

    data object OpenGroups : ViewModelEvent

    data object OpenWorkflowGallery : ViewModelEvent

    data object OpenPrivacy : ViewModelEvent

    data object OpenCloudDrives : ViewModelEvent

    data object OpenWhatsNew : ViewModelEvent

    data class EventAction(
      val value: ResolvedEventAction,
    ) : ViewModelEvent
  }

  enum class EventType(
    @param:StringRes val title: Int,
  ) {
    Reminder(R.string.add_reminder_menu),
    Birthday(R.string.add_birthday),
    GoogleTask(R.string.add_google_task),
    Note(R.string.add_note),
  }

  companion object {
    private const val TAG = "ScheduleHomeViewModel"
  }
}
