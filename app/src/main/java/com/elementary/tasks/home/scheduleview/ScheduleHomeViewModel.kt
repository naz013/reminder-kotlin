package com.elementary.tasks.home.scheduleview

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.eventaction.DispatchEventActionUseCase
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
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class ScheduleHomeViewModel(
  dispatcherProvider: DispatcherProvider,
  private val getActiveEventsForTheDayUseCase: GetActiveEventsForTheDayUseCase,
  private val getTimeSectionsUseCase: GetTimeSectionsUseCase,
  private val getGreetingTextUseCase: GetGreetingTextUseCase,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val getNavigationItemsUseCase: GetNavigationItemsUseCase,
  private val dispatchEventActionUseCase: DispatchEventActionUseCase,
  private val prefs: Prefs,
  private val featureManager: FeatureManager,
  private val whatsNewManager: WhatsNewManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val legalDocumentRepository: LegalDocumentRepository,
) : BaseProgressViewModel(dispatcherProvider) {
  val homeScreenState: StateFlow<HomeScreenState> field = MutableStateFlow(HomeScreenState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadData()
  }

  fun onEventTypeSelected(eventType: EventType) {
    Logger.i(TAG, "On event type selected: type=$eventType")
    when (eventType) {
      EventType.Reminder -> {
        navigationEvent.value = Event(NavigationEvent.OpenCreateReminder)
      }

      EventType.Birthday -> {
        navigationEvent.value = Event(NavigationEvent.OpenCreateBirthday)
      }

      EventType.GoogleTask -> {
        navigationEvent.value = Event(NavigationEvent.OpenCreateGoogleTask)
      }

      EventType.Note -> {
        navigationEvent.value = Event(NavigationEvent.OpenCreateNote)
      }
    }
  }

  fun onPrivacyPolicyClick() {
    Logger.i(TAG, "On privacy policy click.")
    legalDocumentRepository.markSeen(LegalDocumentType.PRIVACY_POLICY)
    navigationEvent.value = Event(NavigationEvent.OpenPrivacy)
    homeScreenState.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onPrivacyAcceptClick() {
    Logger.i(TAG, "On privacy accept click")
    legalDocumentRepository.markSeen(LegalDocumentType.PRIVACY_POLICY)
    homeScreenState.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onLoginDismissClick() {
    Logger.i(TAG, "On login dismiss click")
    prefs.isUserLogged = true
    homeScreenState.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onLoginClick() {
    Logger.i(TAG, "On login click")
    prefs.isUserLogged = true
    homeScreenState.update {
      it.copy(bannerState = getBannerState())
    }
    navigationEvent.value = Event(NavigationEvent.OpenCloudDrives)
  }

  fun onWhatsNewDetailsClick() {
    Logger.i(TAG, "On whats new details click")
    whatsNewManager.hideWhatsNew()
    analyticsEventSender.send(ScreenUsedEvent(Screen.WHATS_NEW))
    homeScreenState.update {
      it.copy(bannerState = getBannerState())
    }
    navigationEvent.value = Event(NavigationEvent.OpenWhatsNew)
  }

  fun onWhatsNewDismissClick() {
    Logger.i(TAG, "On whats new dismiss click")
    whatsNewManager.hideWhatsNew()
    homeScreenState.update {
      it.copy(bannerState = getBannerState())
    }
  }

  fun onSettingsClicked() {
    Logger.i(TAG, "On settings clicked")
    navigationEvent.value = Event(NavigationEvent.OpenSettings)
  }

  fun onEventClicked(homeEvent: HomeEvent) {
    Logger.i(TAG, "On event clicked: id=${homeEvent.id}")
    when (homeEvent.type) {
      HomeEvent.EventType.Reminder -> {
        navigationEvent.value = Event(NavigationEvent.OpenReminderDetails(homeEvent.id))
      }

      HomeEvent.EventType.Birthday -> {
        navigationEvent.value = Event(NavigationEvent.OpenBirthdayDetails(homeEvent.id))
      }
    }
  }

  fun onEventActionClicked(
    context: Context,
    eventAction: HomeEvent.EventAction,
  ) {
    Logger.i(
      TAG,
      "On event action clicked: type=${eventAction::class.java.simpleName}, target=${
        Logger.private(eventAction.toString())
      }",
    )
    dispatchEventActionUseCase(context, eventAction.value)
  }

  fun onHeaderNavigationItemClicked(item: HeaderNavigationItem) {
    Logger.i(TAG, "On header navigation item clicked: ${item.navigationEvent}")
    navigationEvent.value = Event(item.navigationEvent)
  }

  private fun loadData() {
    homeScreenState.update {
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
      homeScreenState.update {
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
      homeScreenState.update {
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

  sealed interface NavigationEvent {
    data class OpenReminderDetails(
      val uuid: String,
    ) : NavigationEvent

    data class OpenBirthdayDetails(
      val uuid: String,
    ) : NavigationEvent

    data object OpenSettings : NavigationEvent

    data class ShowEventTypeSelection(
      val types: List<EventType>,
    ) : NavigationEvent

    data object OpenCreateReminder : NavigationEvent

    data object OpenCreateBirthday : NavigationEvent

    data object OpenCreateGoogleTask : NavigationEvent

    data object OpenCreateNote : NavigationEvent

    data object OpenEvents : NavigationEvent

    data object OpenCalendar : NavigationEvent

    data object OpenNotes : NavigationEvent

    data object OpenGoogleTasks : NavigationEvent

    data object OpenGroups : NavigationEvent

    data object OpenWorkflowGallery : NavigationEvent

    data object OpenPrivacy : NavigationEvent

    data object OpenCloudDrives : NavigationEvent

    data object OpenWhatsNew : NavigationEvent
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
