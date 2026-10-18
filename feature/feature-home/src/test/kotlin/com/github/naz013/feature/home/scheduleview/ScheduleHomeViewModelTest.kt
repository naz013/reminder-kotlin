package com.github.naz013.feature.home.scheduleview

import com.github.naz013.testing.BaseTest
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
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
import com.github.naz013.feature.home.withSelectedEvent
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.system.ProTrialController
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertNull
import com.github.naz013.ui.common.R
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.today.TodayItem
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.threeten.bp.LocalDateTime
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class ScheduleHomeViewModelTest : BaseTest() {
  private val observeTodayScheduleUseCase = mockk<ObserveTodayScheduleUseCase>()
  private val todayItemHomeEventMapper = mockk<TodayItemHomeEventMapper>()
  private val getTimeSectionsUseCase = mockk<GetTimeSectionsUseCase>()
  private val getGreetingTextUseCase = mockk<GetGreetingTextUseCase>()
  private val googleTasksAuthManager = mockk<GoogleTasksAuthManager>()
  private val getNavigationItemsUseCase = mockk<GetNavigationItemsUseCase>()
  private val homePreferences = mockk<HomePreferences>(relaxed = true)
  private val featureFlags = mockk<FeatureFlags>()
  private val whatsNewManager = mockk<WhatsNewManager>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val legalDocumentRepository = mockk<LegalDocumentRepository>(relaxed = true)
  private val buildInfo = mockk<BuildInfo>(relaxed = true)
  private val textProvider = mockk<TextProvider>()
  private val hasAnyUserContentUseCase = mockk<HasAnyUserContentUseCase>()
  private val proTrialController = mockk<ProTrialController>()
  private val remainingMillis = MutableStateFlow(0L)

  private lateinit var viewModel: ScheduleHomeViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // GetGreetingTextUseCase.invoke has a `time: LocalTime = LocalTime.now()` default param,
    // evaluated fresh at every call site - stubbing the bare `()` call captures one exact
    // instant, which won't match the real call's (different) instant. Use any() instead.
    every { getGreetingTextUseCase(any()) } returns "Good morning"
    every { googleTasksAuthManager.isAuthorized() } returns true
    // Pro by default so existing menu/gating expectations don't all need to opt in; tests that
    // care about the free-user experience override this locally.
    every { buildInfo.isPro } returns true
    every { proTrialController.observeRemainingMillis() } returns remainingMillis
    every { observeTodayScheduleUseCase() } returns flowOf(snapshot())
    coEvery { todayItemHomeEventMapper(any(), any()) } returns emptyList()
    every { getTimeSectionsUseCase(any()) } returns emptyList()
    coEvery { getNavigationItemsUseCase(any(), any()) } returns emptyList()
    every { legalDocumentRepository.hasUpdate(any()) } returns false
    every { homePreferences.isUserLogged } returns true
    every { featureFlags.isEnabled(any()) } returns false
    every { whatsNewManager.hasChanges() } returns false
    coEvery { hasAnyUserContentUseCase() } returns true
    every { textProvider.getQuantityString(any(), any()) } answers {
      val label = when (firstArg<Int>()) {
        R.plurals.home_summary_left -> "left"
        R.plurals.home_summary_done -> "done"
        else -> "overdue"
      }
      "${secondArg<Int>()} $label"
    }

    viewModel =
      ScheduleHomeViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        observeTodayScheduleUseCase = observeTodayScheduleUseCase,
        todayItemHomeEventMapper = todayItemHomeEventMapper,
        getTimeSectionsUseCase = getTimeSectionsUseCase,
        getGreetingTextUseCase = getGreetingTextUseCase,
        googleTasksAuthManager = googleTasksAuthManager,
        getNavigationItemsUseCase = getNavigationItemsUseCase,
        homePreferences = homePreferences,
        featureFlags = featureFlags,
        whatsNewManager = whatsNewManager,
        analyticsEventSender = analyticsEventSender,
        legalDocumentRepository = legalDocumentRepository,
        buildInfo = buildInfo,
        textProvider = textProvider,
        hasAnyUserContentUseCase = hasAnyUserContentUseCase,
        proTrialController = proTrialController,
      )
  }

  @Test
  fun `proTrialRemaining is null when the pro trial is not running`() = runTest {
    val state = viewModel.state.first()

    assertNull(state.proTrialRemaining)
  }

  @Test
  fun `proTrialRemaining shows hours and minutes while the pro trial is running`() = runTest {
    remainingMillis.value = TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(5)

    val state = viewModel.state.first()

    assertEquals("03:05", state.proTrialRemaining)
  }

  @Test
  fun `proTrialRemaining is cleared when the pro trial expires`() = runTest {
    remainingMillis.value = TimeUnit.HOURS.toMillis(1)
    val states = mutableListOf<HomeScreenState>()
    val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }

    remainingMillis.value = 0L

    assertNull(states.last().proTrialRemaining)
    job.cancel()
  }

  @Test
  fun `add menu offers quick add once the pro trial makes the user pro`() = runTest {
    every { buildInfo.isPro } returns false
    val states = mutableListOf<HomeScreenState>()
    val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }
    assertFalse(states.last().addMenuItems.contains(ScheduleHomeViewModel.EventType.QuickAdd))

    every { buildInfo.isPro } returns true
    remainingMillis.value = TimeUnit.HOURS.toMillis(1)

    assertTrue(states.last().addMenuItems.contains(ScheduleHomeViewModel.EventType.QuickAdd))
    job.cancel()
  }

  @Test
  fun `loadData populates greeting, menu items, and header navigation items`() =
    runTest {
      coEvery { getNavigationItemsUseCase(any(), any()) } returns
        listOf(mockk<HeaderNavigationItem>())

      val state = viewModel.state.first()

      assertEquals("Good morning", state.greeting)
      assertEquals(1, state.headerNavigationItems.size)
      assertEquals(ScheduleHomeViewModel.EventType.entries, state.addMenuItems)
    }

  @Test
  fun `loadData restricts the add menu when Google Tasks is not authorized`() =
    runTest {
      every { googleTasksAuthManager.isAuthorized() } returns false

      val state = viewModel.state.first()

      assertEquals(
        listOf(
          ScheduleHomeViewModel.EventType.QuickAdd,
          ScheduleHomeViewModel.EventType.Reminder,
          ScheduleHomeViewModel.EventType.Birthday,
          ScheduleHomeViewModel.EventType.Note,
          ScheduleHomeViewModel.EventType.Todo,
        ),
        state.addMenuItems,
      )
    }

  @Test
  fun `loadData hides quick add from the menu for free users`() =
    runTest {
      every { buildInfo.isPro } returns false

      val state = viewModel.state.first()

      assertEquals(
        listOf(
          ScheduleHomeViewModel.EventType.Reminder,
          ScheduleHomeViewModel.EventType.Birthday,
          ScheduleHomeViewModel.EventType.GoogleTask,
          ScheduleHomeViewModel.EventType.Note,
          ScheduleHomeViewModel.EventType.Todo,
        ),
        state.addMenuItems,
      )
    }

  @Test
  fun `loadData shows an empty list state when there are no sections`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(ListState.Empty(EmptyKind.NothingPlanned), state.listState)
    }

  @Test
  fun `loadData marks the empty state as first run when the user has no content at all`() =
    runTest {
      coEvery { hasAnyUserContentUseCase() } returns false

      val state = viewModel.state.first()

      assertEquals(ListState.Empty(EmptyKind.FirstRun), state.listState)
      assertEquals("", state.daySummary)
    }

  @Test
  fun `loadData does not mark a clear day as first run when the user has content`() =
    runTest {
      coEvery { hasAnyUserContentUseCase() } returns true

      val state = viewModel.state.first()

      assertEquals(ListState.Empty(EmptyKind.NothingPlanned), state.listState)
    }

  @Test
  fun `loadData builds the day summary from left, done and overdue counts`() =
    runTest {
      val items = listOf(
        birthdayItem("done1", TodayBucket.Timed, NOW.minusHours(2)),
        birthdayItem("done2", TodayBucket.Timed, NOW.minusHours(1)),
        birthdayItem("timed", TodayBucket.Timed, NOW.plusHours(1)),
        birthdayItem("allDay", TodayBucket.AllDay, NOW.toLocalDate().atStartOfDay()),
        birthdayItem("anytime", TodayBucket.Anytime, NOW.toLocalDate().atStartOfDay()),
        overdueReminder(),
      )
      stubSchedule(items)

      val state = viewModel.state.first()

      assertEquals("3 left · 2 done · 1 overdue", state.daySummary)
    }

  @Test
  fun `loadData omits zero counts from the day summary`() =
    runTest {
      stubSchedule(listOf(birthdayItem("anytime", TodayBucket.Anytime, NOW.toLocalDate().atStartOfDay())))

      val state = viewModel.state.first()

      assertEquals("1 left", state.daySummary)
    }

  @Test
  fun `loadData extracts earlier day reminders into the overdue strip only`() =
    runTest {
      val overdue = overdueReminder("late")
      val timed = birthdayItem("timed", TodayBucket.Timed, NOW.plusHours(1))
      stubSchedule(listOf(overdue, timed))

      val state = viewModel.state.first()

      val schedule = (state.listState as ListState.Ready).schedule
      assertEquals(listOf("late"), schedule.overdue.map { it.id })
      assertEquals(listOf("timed"), schedule.upcomingTimed.map { it.event.id })
      assertTrue(schedule.earlierTimed.isEmpty())
      assertEquals(1, schedule.overdueCount)
    }

  @Test
  fun `loadData shows a ready state when only overdue reminders exist`() =
    runTest {
      stubSchedule(listOf(overdueReminder("late")))

      val state = viewModel.state.first()

      val schedule = (state.listState as ListState.Ready).schedule
      assertEquals(listOf("late"), schedule.overdue.map { it.id })
      assertEquals("1 overdue", state.daySummary)
    }

  @Test
  fun `onOverdueToggled expands and re-collapses the overdue strip`() =
    runTest {
      stubSchedule(listOf(overdueReminder("late")))
      val states = mutableListOf<HomeScreenState>()
      val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }

      val initial = (states.last().listState as ListState.Ready).schedule.isOverdueExpanded
      viewModel.onOverdueToggled()
      val afterFirst = (states.last().listState as ListState.Ready).schedule.isOverdueExpanded
      viewModel.onOverdueToggled()
      val afterSecond = (states.last().listState as ListState.Ready).schedule.isOverdueExpanded
      job.cancel()

      assertFalse(initial)
      assertTrue(afterFirst)
      assertFalse(afterSecond)
    }

  @Test
  fun `loadData shows a ready list state with time sections when events exist`() =
    runTest {
      val event =
        HomeEvent(
          id = "r1",
          text = "Buy milk",
          description = null,
          groupName = null,
          remaining = null,
          color = androidx.compose.ui.graphics.Color.Black,
          action = null,
          date = LocalDate.of(2026, 7, 15),
          time = LocalTime.of(9, 0),
          type = HomeEvent.EventType.Reminder,
        )
      val item = birthdayItem("r1", TodayBucket.Timed, NOW.plusHours(1))
      every { observeTodayScheduleUseCase() } returns flowOf(snapshot(item))
      coEvery { todayItemHomeEventMapper(listOf(item), NOW) } returns listOf(event)
      every { getTimeSectionsUseCase(listOf(event)) } returns
        listOf(com.github.naz013.feature.home.TimeSection(time = "9:00", event = event))

      val state = viewModel.state.first()

      val listState = state.listState
      assertTrue(listState is ListState.Ready)
      assertEquals(1, (listState as ListState.Ready).schedule.upcomingTimed.size)
    }

  @Test
  fun `loadData splits timed items into earlier and upcoming and keeps all-day and anytime buckets`() =
    runTest {
      val past = birthdayItem("past", TodayBucket.Timed, NOW.minusHours(2))
      val future = birthdayItem("future", TodayBucket.Timed, NOW.plusHours(2))
      val allDay = birthdayItem("allDay", TodayBucket.AllDay, NOW.toLocalDate().atStartOfDay())
      val anytime = birthdayItem("anytime", TodayBucket.Anytime, NOW.toLocalDate().atStartOfDay())
      every { observeTodayScheduleUseCase() } returns flowOf(snapshot(allDay, past, future, anytime))
      listOf(past, future, allDay, anytime).forEach { item ->
        coEvery { todayItemHomeEventMapper(listOf(item), NOW) } returns listOf(homeEvent(item.id))
      }
      every { getTimeSectionsUseCase(any()) } answers {
        firstArg<List<HomeEvent>>().map { TimeSection(time = "t", event = it) }
      }

      val state = viewModel.state.first()

      val schedule = (state.listState as ListState.Ready).schedule
      assertEquals(listOf("past"), schedule.earlierTimed.map { it.event.id })
      assertEquals(listOf("future"), schedule.upcomingTimed.map { it.event.id })
      assertEquals(listOf("allDay"), schedule.allDay.map { it.id })
      assertEquals(listOf("anytime"), schedule.anytime.map { it.id })
      assertEquals(1, schedule.earlierCount)
      assertTrue(schedule.showNowMarker)
      assertFalse(schedule.isEarlierExpanded)
    }

  @Test
  fun `loadData shows a ready state when only anytime items exist`() =
    runTest {
      val anytime = birthdayItem("anytime", TodayBucket.Anytime, NOW.toLocalDate().atStartOfDay())
      every { observeTodayScheduleUseCase() } returns flowOf(snapshot(anytime))
      coEvery { todayItemHomeEventMapper(any(), any()) } returns listOf(homeEvent("anytime"))

      val state = viewModel.state.first()

      val schedule = (state.listState as ListState.Ready).schedule
      assertEquals(listOf("anytime"), schedule.anytime.map { it.id })
      assertFalse(schedule.showNowMarker)
    }

  @Test
  fun `withSelectedEvent marks all-day and earlier timed events as selected`() {
    val schedule = TodaySchedule(
      earlierTimed = listOf(TimeSection("t", homeEvent("early"))),
      allDay = listOf(homeEvent("allDay")),
    )
    val state = HomeScreenState(listState = ListState.Ready(schedule))

    val allDaySelected = (state.withSelectedEvent("allDay").listState as ListState.Ready).schedule
    val earlierSelected = (state.withSelectedEvent("early").listState as ListState.Ready).schedule

    assertTrue(allDaySelected.allDay.single().isSelected)
    assertFalse(allDaySelected.earlierTimed.single().event.isSelected)
    assertTrue(earlierSelected.earlierTimed.single().event.isSelected)
    assertFalse(earlierSelected.allDay.single().isSelected)
  }

  @Test
  fun `loadData does not show the now marker when the day has only all-day items`() =
    runTest {
      val allDay = birthdayItem("allDay", TodayBucket.AllDay, NOW.toLocalDate().atStartOfDay())
      every { observeTodayScheduleUseCase() } returns flowOf(snapshot(allDay))
      coEvery { todayItemHomeEventMapper(any(), any()) } returns listOf(homeEvent("allDay"))

      val state = viewModel.state.first()

      val schedule = (state.listState as ListState.Ready).schedule
      assertFalse(schedule.showNowMarker)
      assertFalse(schedule.showEarlierHeader)
      assertEquals(listOf("allDay"), schedule.allDay.map { it.id })
    }

  @Test
  fun `loadData shows a ready list state when only calendar or task items exist`() =
    runTest {
      val task = TodayItem.Task(mockk(), NOW, TodayBucket.Anytime)
      every { observeTodayScheduleUseCase() } returns flowOf(snapshot(task))
      coEvery { todayItemHomeEventMapper(any(), any()) } returns listOf(homeEvent("task"))

      val state = viewModel.state.first()

      assertEquals(listOf("task"), (state.listState as ListState.Ready).schedule.anytime.map { it.id })
    }

  @Test
  fun `onEarlierTodayToggled flips the earlier today expanded flag`() =
    runTest {
      val past = birthdayItem("past", TodayBucket.Timed, NOW.minusHours(2))
      every { observeTodayScheduleUseCase() } returns flowOf(snapshot(past))
      coEvery { todayItemHomeEventMapper(any(), any()) } returns listOf(homeEvent("x"))
      every { getTimeSectionsUseCase(any()) } answers {
        firstArg<List<HomeEvent>>().map { TimeSection(time = "t", event = it) }
      }
      val states = mutableListOf<HomeScreenState>()
      val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }

      viewModel.onEarlierTodayToggled()
      val expandedAfterFirst = (states.last().listState as ListState.Ready).schedule.isEarlierExpanded
      viewModel.onEarlierTodayToggled()
      val expandedAfterSecond = (states.last().listState as ListState.Ready).schedule.isEarlierExpanded
      job.cancel()

      assertTrue(expandedAfterFirst)
      assertFalse(expandedAfterSecond)
    }

  @Test
  fun `loadData shows the privacy banner when the privacy policy has an update`() =
    runTest {
      every { legalDocumentRepository.hasUpdate(LegalDocumentType.PRIVACY_POLICY) } returns true

      val state = viewModel.state.first()

      assertEquals(BannerState.Privacy, state.bannerState)
    }

  @Test
  fun `loadData shows the login banner when the user is logged out and google drive is enabled`() =
    runTest {
      every { homePreferences.isUserLogged } returns false
      every { featureFlags.isEnabled(FeatureFlag.GOOGLE_DRIVE) } returns true

      val state = viewModel.state.first()

      assertEquals(BannerState.Login, state.bannerState)
    }

  @Test
  fun `loadData shows the whats-new banner when there are changes and no higher-priority banner`() =
    runTest {
      every { whatsNewManager.hasChanges() } returns true

      val state = viewModel.state.first()

      assertEquals(BannerState.WhatsNew, state.bannerState)
    }

  @Test
  fun `loadData shows no banner when nothing applies`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(null, state.bannerState)
    }

  @Test
  fun `onEventTypeSelected Reminder posts OpenCreateReminder`() {
    viewModel.onEventTypeSelected(ScheduleHomeViewModel.EventType.Reminder)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenCreateReminder,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventTypeSelected Birthday posts OpenCreateBirthday`() {
    viewModel.onEventTypeSelected(ScheduleHomeViewModel.EventType.Birthday)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenCreateBirthday,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventTypeSelected GoogleTask posts OpenCreateGoogleTask`() {
    viewModel.onEventTypeSelected(ScheduleHomeViewModel.EventType.GoogleTask)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenCreateGoogleTask,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventTypeSelected Note posts OpenCreateNote`() {
    viewModel.onEventTypeSelected(ScheduleHomeViewModel.EventType.Note)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenCreateNote,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventTypeSelected QuickAdd posts OpenQuickAdd for pro users`() {
    every { buildInfo.isPro } returns true

    viewModel.onEventTypeSelected(ScheduleHomeViewModel.EventType.QuickAdd)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenQuickAdd,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventTypeSelected QuickAdd posts OpenProVersion and sends a gate event for free users`() {
    every { buildInfo.isPro } returns false

    viewModel.onEventTypeSelected(ScheduleHomeViewModel.EventType.QuickAdd)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenProVersion,
      viewModel.event.value?.peekContent(),
    )
    verify { analyticsEventSender.send(FeatureGateTappedEvent(Feature.QUICK_ADD)) }
  }

  @Test
  fun `onPrivacyPolicyClick marks the policy seen and posts OpenPrivacy`() =
    runTest {
      viewModel.onPrivacyPolicyClick()

      verify { legalDocumentRepository.markSeen(LegalDocumentType.PRIVACY_POLICY) }
      assertEquals(
        ScheduleHomeViewModel.ViewModelEvent.OpenPrivacy,
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `onPrivacyAcceptClick marks the policy seen without posting an event`() {
    viewModel.onPrivacyAcceptClick()

    verify { legalDocumentRepository.markSeen(LegalDocumentType.PRIVACY_POLICY) }
    assertEquals(null, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onLoginDismissClick marks the user logged in without posting an event`() {
    viewModel.onLoginDismissClick()

    verify { homePreferences.isUserLogged = true }
    assertEquals(null, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onLoginClick marks the user logged in and posts OpenCloudDrives`() {
    viewModel.onLoginClick()

    verify { homePreferences.isUserLogged = true }
    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenCloudDrives,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onWhatsNewDetailsClick hides whats-new, sends analytics, and posts OpenWhatsNew`() {
    viewModel.onWhatsNewDetailsClick()

    verify { whatsNewManager.hideWhatsNew() }
    verify { analyticsEventSender.send(any()) }
    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenWhatsNew,
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onWhatsNewDismissClick hides whats-new without posting an event`() {
    viewModel.onWhatsNewDismissClick()

    verify { whatsNewManager.hideWhatsNew() }
    assertEquals(null, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onSettingsClicked posts OpenSettings`() {
    viewModel.onSettingsClicked()

    assertEquals(ScheduleHomeViewModel.ViewModelEvent.OpenSettings, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onEventClicked on a reminder posts OpenReminderDetails`() {
    val event =
      HomeEvent(
        id = "r1",
        text = null,
        description = null,
        groupName = null,
        remaining = null,
        color = androidx.compose.ui.graphics.Color.Black,
        action = null,
        date = LocalDate.now(),
        time = LocalTime.now(),
        type = HomeEvent.EventType.Reminder,
      )

    viewModel.onEventClicked(event)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenReminderDetails("r1"),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventClicked on a birthday posts OpenBirthdayDetails`() {
    val event =
      HomeEvent(
        id = "b1",
        text = null,
        description = null,
        groupName = null,
        remaining = null,
        color = androidx.compose.ui.graphics.Color.Black,
        action = null,
        date = LocalDate.now(),
        time = LocalTime.now(),
        type = HomeEvent.EventType.Birthday,
      )

    viewModel.onEventClicked(event)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenBirthdayDetails("b1"),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEventClicked on a calendar event posts OpenCalendar and on a task posts OpenGoogleTasks`() {
    viewModel.onEventClicked(homeEvent("c1").copy(type = HomeEvent.EventType.CalendarEvent))
    assertEquals(ScheduleHomeViewModel.ViewModelEvent.OpenCalendar, viewModel.event.value?.peekContent())

    viewModel.onEventClicked(homeEvent("t1").copy(type = HomeEvent.EventType.Task))
    assertEquals(ScheduleHomeViewModel.ViewModelEvent.OpenGoogleTasks, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onEventActionClicked posts an EventAction event with the resolved action`() {
    val resolvedAction = mockk<ResolvedEventAction>()
    val action = HomeEvent.EventAction(icon = 1, value = resolvedAction)

    viewModel.onEventActionClicked(action)

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.EventAction(resolvedAction),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onHeaderNavigationItemClicked posts the item's navigation event`() {
    val item =
      HeaderNavigationItem(
        titleRes = 1,
        iconRes = 1,
        color = androidx.compose.ui.graphics.Color.Black,
        navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenCalendar,
        subtitle = "",
      )

    viewModel.onHeaderNavigationItemClicked(item)

    assertEquals(ScheduleHomeViewModel.ViewModelEvent.OpenCalendar, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onHeaderNavigationItemLongClicked posts OpenHeaderItemsSettings`() {
    viewModel.onHeaderNavigationItemLongClicked()

    assertEquals(
      ScheduleHomeViewModel.ViewModelEvent.OpenHeaderItemsSettings,
      viewModel.event.value?.peekContent(),
    )
  }

  private fun snapshot(vararg items: TodayItem) = TodaySnapshot(items.toList(), NOW)

  private fun overdueReminder(id: String = "overdue"): TodayItem =
    mockk<TodayItem.Reminder>(relaxed = true) {
      every { this@mockk.id } returns id
      every { bucket } returns TodayBucket.Timed
      every { dateTime } returns NOW.minusDays(1)
      every { isOverdue } returns true
    }

  private fun stubSchedule(items: List<TodayItem>) {
    every { observeTodayScheduleUseCase() } returns flowOf(TodaySnapshot(items, NOW))
    coEvery { todayItemHomeEventMapper(any(), any()) } answers {
      firstArg<List<TodayItem>>().map { homeEvent(it.id) }
    }
    every { getTimeSectionsUseCase(any()) } answers {
      firstArg<List<HomeEvent>>().map { TimeSection(time = "t", event = it) }
    }
  }

  private fun homeEvent(id: String) =
    HomeEvent(
      id = id,
      text = null,
      description = null,
      groupName = null,
      remaining = null,
      color = androidx.compose.ui.graphics.Color.Black,
      action = null,
      date = NOW.toLocalDate(),
      time = NOW.toLocalTime(),
      type = HomeEvent.EventType.Reminder,
    )

  private fun birthdayItem(
    id: String,
    bucket: TodayBucket,
    dateTime: LocalDateTime,
  ) = TodayItem.Birthday(Birthday(uuId = id, name = id, syncState = SyncState.Synced), dateTime, bucket)

  companion object {
    private val NOW = LocalDateTime.of(2026, 7, 15, 12, 0)
  }
}
