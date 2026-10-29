package com.github.naz013.feature.home.scheduleview

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.feature.home.ResolvedEventAction
import com.github.naz013.feature.home.BannerState
import com.github.naz013.feature.home.EmptyKind
import com.github.naz013.feature.home.HeaderNavigationItem
import com.github.naz013.feature.home.HomeEvent
import com.github.naz013.feature.home.HomeScreenState
import com.github.naz013.feature.home.ListState
import com.github.naz013.feature.home.TimeSection
import com.github.naz013.feature.home.TodaySchedule
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.usecase.RescheduleRemindersUseCase
import com.github.naz013.logic.reminder.usecase.RestoreReminderStatesUseCase
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.feature.home.withSelectedEvent
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.ProTrialController
import com.github.naz013.common.system.observeIsActive
import com.github.naz013.platform.BuildInfo
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime

class ScheduleHomeViewModel internal constructor(
  private val dispatcherProvider: DispatcherProvider,
  private val observeTodayScheduleUseCase: ObserveTodayScheduleUseCase,
  private val todayItemHomeEventMapper: TodayItemHomeEventMapper,
  private val getTimeSectionsUseCase: GetTimeSectionsUseCase,
  private val getGreetingTextUseCase: GetGreetingTextUseCase,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val getNavigationItemsUseCase: GetNavigationItemsUseCase,
  private val homePreferences: HomePreferences,
  private val featureFlags: FeatureFlags,
  private val whatsNewManager: WhatsNewManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val legalDocumentRepository: LegalDocumentRepository,
  private val buildInfo: BuildInfo,
  private val textProvider: TextProvider,
  private val hasAnyUserContentUseCase: HasAnyUserContentUseCase,
  private val proTrialController: ProTrialController,
  private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
  private val restoreReminderStatesUseCase: RestoreReminderStatesUseCase,
) : ViewModel() {
  private var proTrialJob: Job? = null
  private var lastRescheduledStates: List<RestoreReminderStatesUseCase.State> = emptyList()
  private var headerItemsJob: Job? = null

  private val _state = MutableStateFlow(HomeScreenState())
  private val _selectedEventId = MutableStateFlow<String?>(null)
  private val _isEarlierExpanded = MutableStateFlow(false)
  private val _isOverdueExpanded = MutableStateFlow(false)
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

      EventType.QuickAdd -> {
        if (buildInfo.isPro) {
          event.emit(ViewModelEvent.OpenQuickAdd)
        } else {
          analyticsEventSender.send(FeatureGateTappedEvent(Feature.QUICK_ADD))
          event.emit(ViewModelEvent.OpenProVersion)
        }
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

      HomeEvent.EventType.CalendarEvent -> {
        event.emit(ViewModelEvent.OpenCalendar)
      }

      HomeEvent.EventType.Task -> {
        event.emit(ViewModelEvent.OpenGoogleTasks)
      }
    }
  }

  fun onEarlierTodayToggled() {
    Logger.i(TAG, "On earlier today toggled")
    _isEarlierExpanded.update { !it }
  }

  fun onOverdueToggled() {
    Logger.i(TAG, "On overdue toggled")
    _isOverdueExpanded.update { !it }
  }

  fun onRescheduleAllClicked() {
    Logger.i(TAG, "On reschedule all clicked")
    if (overdueIds().isEmpty()) return
    _state.update { it.copy(isRescheduleSheetVisible = true) }
  }

  fun onRescheduleDismissed() {
    _state.update { it.copy(isRescheduleSheetVisible = false) }
  }

  fun onRescheduleTodayClicked() {
    reschedule(RescheduleRemindersUseCase.Target.Today)
  }

  fun onRescheduleTomorrowClicked() {
    reschedule(RescheduleRemindersUseCase.Target.Tomorrow)
  }

  fun onReschedulePicked(dateTime: LocalDateTime) {
    reschedule(RescheduleRemindersUseCase.Target.Picked(dateTime))
  }

  fun onRescheduleUndoClicked() {
    Logger.i(TAG, "On reschedule undo clicked")
    val states = lastRescheduledStates
    lastRescheduledStates = emptyList()
    if (states.isEmpty()) return
    viewModelScope.launch(dispatcherProvider.default()) {
      restoreReminderStatesUseCase(states)
    }
  }

  private fun overdueIds(): List<String> =
    (_state.value.listState as? ListState.Ready)?.schedule?.overdue.orEmpty().map { it.id }

  private fun reschedule(target: RescheduleRemindersUseCase.Target) {
    Logger.i(TAG, "Reschedule overdue: target=$target")
    _state.update { it.copy(isRescheduleSheetVisible = false) }
    val ids = overdueIds()
    if (ids.isEmpty()) return
    viewModelScope.launch(dispatcherProvider.default()) {
      val before = restoreReminderStatesUseCase.capture(ids)
      val summary = rescheduleRemindersUseCase(ids, target)
      val skippedIds = summary.skipped.map { it.id }.toSet()
      lastRescheduledStates = before.filter { it.id !in skippedIds }
      val message = rescheduleMessage(summary)
      val canUndo = lastRescheduledStates.isNotEmpty()
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.ShowRescheduleResult(message = message, canUndo = canUndo))
      }
    }
  }

  private fun rescheduleMessage(summary: RescheduleRemindersUseCase.Summary): String {
    if (summary.moved == 0 && summary.skipped.isEmpty()) {
      return textProvider.getString(R.string.reschedule_summary_nothing_moved)
    }
    val movedText = if (summary.moved > 0) {
      textProvider.getQuantityString(R.plurals.reschedule_summary_moved, summary.moved)
    } else {
      null
    }
    val skippedText = if (summary.skipped.isNotEmpty()) {
      val count = textProvider.getQuantityString(R.plurals.reschedule_summary_skipped, summary.skipped.size)
      summary.mostCommonSkipReason?.let {
        textProvider.getString(R.string.reschedule_summary_with_reason, count, textProvider.getString(skipReasonRes(it)))
      } ?: count
    } else {
      null
    }
    return listOfNotNull(movedText, skippedText).joinToString(SUMMARY_PART_SEPARATOR)
  }

  @StringRes
  private fun skipReasonRes(reason: RescheduleRemindersUseCase.SkipReason): Int =
    when (reason) {
      RescheduleRemindersUseCase.SkipReason.UNSUPPORTED_TYPE -> R.string.reschedule_skip_unsupported_type
      RescheduleRemindersUseCase.SkipReason.NOT_FOUND -> R.string.reschedule_skip_not_found
      RescheduleRemindersUseCase.SkipReason.NOTHING_TO_MOVE -> R.string.reschedule_skip_nothing_to_move
      RescheduleRemindersUseCase.SkipReason.STILL_OVERDUE -> R.string.reschedule_skip_still_overdue
      RescheduleRemindersUseCase.SkipReason.TARGET_IN_PAST -> R.string.reschedule_skip_target_in_past
      RescheduleRemindersUseCase.SkipReason.NOT_ACTIVATED -> R.string.reschedule_skip_not_activated
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
    if (item.navigationEvent == ViewModelEvent.OpenPomodoro && !buildInfo.isPro) {
      analyticsEventSender.send(FeatureGateTappedEvent(Feature.POMODORO))
      event.emit(ViewModelEvent.OpenProVersion)
    } else {
      event.emit(item.navigationEvent)
    }
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
        addMenuItems = EventType.entries.filter { isAddMenuItemAvailable(it) },
        bannerState = getBannerState(),
      )
    }
    observeProTrial()
    loadHeaderNavigationItems()
    viewModelScope.launch(dispatcherProvider.default()) {
      val loadedToday = observeTodayScheduleUseCase().map { snapshot -> loadToday(snapshot) }
      combine(loadedToday, _isEarlierExpanded, _isOverdueExpanded) { loaded, earlierExpanded, overdueExpanded ->
          toRendered(loaded, earlierExpanded, overdueExpanded)
        }
        .collect { rendered ->
          _state.update { it.copy(listState = rendered.listState, daySummary = rendered.daySummary) }
        }
    }
  }

  private fun observeProTrial() {
    proTrialJob?.cancel()
    proTrialJob = viewModelScope.launch {
      proTrialController.observeIsActive().collectIndexed { index, _ ->
        _state.update {
          it.copy(addMenuItems = EventType.entries.filter { type -> isAddMenuItemAvailable(type) })
        }
        // The first emission is the current status, already covered by loadData's own load.
        if (index > 0) loadHeaderNavigationItems()
      }
    }
  }

  private fun loadHeaderNavigationItems() {
    headerItemsJob?.cancel()
    headerItemsJob = viewModelScope.launch(dispatcherProvider.io()) {
      val items = getNavigationItemsUseCase(this, LocalDateTime.now())
      _state.update { it.copy(headerNavigationItems = items) }
    }
  }

  private suspend fun loadToday(snapshot: TodaySnapshot): LoadedToday {
    Logger.d(TAG, "Loaded ${snapshot.items.size} today items")
    val (allItems, now) = snapshot
    val (overdueItems, items) = allItems.partition { it is TodayItem.Reminder && it.isOverdue }
    val (earlier, upcoming) = items
      .filter { it.bucket == TodayBucket.Timed }
      .partition { it.dateTime.isBefore(now) }
    val loaded = LoadedToday(
      earlierTimed = getTimeSectionsUseCase(mapSorted(earlier, now)),
      upcomingTimed = getTimeSectionsUseCase(mapSorted(upcoming, now)),
      allDay = mapSorted(items.filter { it.bucket == TodayBucket.AllDay }, now),
      anytime = mapSorted(items.filter { it.bucket == TodayBucket.Anytime }, now),
      overdue = mapSorted(overdueItems, now),
    )
    return loaded.copy(
      isFirstRun = loaded.isEmpty && !hasAnyUserContentUseCase(),
    )
  }

  /**
   * left = upcoming timed + all-day + anytime items of today; done = timed items of today whose
   * time has passed; overdue = reminders from earlier days. Zero counts are omitted.
   */
  private fun buildDaySummary(loaded: LoadedToday): String {
    val left = loaded.upcomingTimed.size + loaded.allDay.size + loaded.anytime.size
    val done = loaded.earlierTimed.size
    val overdue = loaded.overdue.size
    return listOf(
      R.plurals.home_summary_left to left,
      R.plurals.home_summary_done to done,
      R.plurals.home_summary_overdue to overdue,
    )
      .filter { (_, count) -> count > 0 }
      .joinToString(SUMMARY_SEPARATOR) { (id, count) -> textProvider.getQuantityString(id, count) }
  }

  private suspend fun mapSorted(items: List<TodayItem>, now: LocalDateTime): List<HomeEvent> {
    if (items.isEmpty()) return emptyList()
    return todayItemHomeEventMapper(items, now).sortedBy { it.date.atTime(it.time) }
  }

  private fun toRendered(
    loaded: LoadedToday,
    isEarlierExpanded: Boolean,
    isOverdueExpanded: Boolean,
  ): RenderedToday {
    if (loaded.isEmpty) return RenderedToday(ListState.Empty(
      if (loaded.isFirstRun) EmptyKind.FirstRun else EmptyKind.NothingPlanned,
    ), "")
    val schedule = TodaySchedule(
      earlierTimed = loaded.earlierTimed,
      upcomingTimed = loaded.upcomingTimed,
      allDay = loaded.allDay,
      anytime = loaded.anytime,
      isEarlierExpanded = isEarlierExpanded,
      overdue = loaded.overdue,
      isOverdueExpanded = isOverdueExpanded,
    )
    return RenderedToday(ListState.Ready(schedule), buildDaySummary(loaded))
  }

  private data class RenderedToday(
    val listState: ListState,
    val daySummary: String,
  )

  private data class LoadedToday(
    val earlierTimed: List<TimeSection>,
    val upcomingTimed: List<TimeSection>,
    val allDay: List<HomeEvent>,
    val anytime: List<HomeEvent>,
    val overdue: List<HomeEvent>,
    val isFirstRun: Boolean = false,
  ) {
    val isEmpty: Boolean
      get() = earlierTimed.isEmpty() && upcomingTimed.isEmpty() && allDay.isEmpty() &&
        anytime.isEmpty() && overdue.isEmpty()
  }

  /** Quick add is PRO-only and hidden from the menu entirely for free users - unlike Pomodoro's
   * "always show, gate on tap" pattern, elsewhere in this same class - rather than shown-but-locked. */
  private fun isAddMenuItemAvailable(eventType: EventType): Boolean =
    when (eventType) {
      EventType.QuickAdd -> buildInfo.isPro
      EventType.GoogleTask -> googleTasksAuthManager.isAuthorized()
      else -> true
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

    data object OpenTags : ViewModelEvent

    data object OpenRoutines : ViewModelEvent

    data object OpenHabits : ViewModelEvent

    data object OpenWorkflowGallery : ViewModelEvent

    data object OpenPomodoro : ViewModelEvent

    data object OpenQuickAdd : ViewModelEvent

    data object OpenProVersion : ViewModelEvent

    data object OpenPrivacy : ViewModelEvent

    data object OpenCloudDrives : ViewModelEvent

    data object OpenWhatsNew : ViewModelEvent

    data class EventAction(
      val value: ResolvedEventAction,
    ) : ViewModelEvent

    data object OpenCreateTodo : ViewModelEvent

    data class ShowRescheduleResult(
      val message: String,
      val canUndo: Boolean,
    ) : ViewModelEvent
  }

  enum class EventType(
    @param:StringRes val title: Int,
  ) {
    QuickAdd(R.string.quick_add),
    Reminder(R.string.reminder),
    Birthday(R.string.birthday),
    GoogleTask(R.string.google_task),
    Note(R.string.note),
    Todo(R.string.todo)
  }

  companion object {
    private const val TAG = "ScheduleHomeViewModel"
    private const val SUMMARY_SEPARATOR = " · "
    private const val SUMMARY_PART_SEPARATOR = ", "
  }
}
