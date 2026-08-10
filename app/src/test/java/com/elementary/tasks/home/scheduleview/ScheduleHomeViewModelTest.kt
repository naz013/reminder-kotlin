package com.elementary.tasks.home.scheduleview

import com.elementary.tasks.BaseTest
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.eventaction.ResolvedEventAction
import com.elementary.tasks.home.BannerState
import com.elementary.tasks.home.HeaderNavigationItem
import com.elementary.tasks.home.HomeEvent
import com.elementary.tasks.home.HomeScreenState
import com.elementary.tasks.home.ListState
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.whatsnew.WhatsNewManager
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class ScheduleHomeViewModelTest : BaseTest() {
  private val getActiveEventsForTheDayUseCase = mockk<GetActiveEventsForTheDayUseCase>()
  private val getTimeSectionsUseCase = mockk<GetTimeSectionsUseCase>()
  private val getGreetingTextUseCase = mockk<GetGreetingTextUseCase>()
  private val googleTasksAuthManager = mockk<GoogleTasksAuthManager>()
  private val getNavigationItemsUseCase = mockk<GetNavigationItemsUseCase>()
  private val prefs = mockk<Prefs>(relaxed = true)
  private val featureFlags = mockk<FeatureFlags>()
  private val whatsNewManager = mockk<WhatsNewManager>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val legalDocumentRepository = mockk<LegalDocumentRepository>(relaxed = true)

  private lateinit var viewModel: ScheduleHomeViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // GetGreetingTextUseCase.invoke has a `time: LocalTime = LocalTime.now()` default param,
    // evaluated fresh at every call site - stubbing the bare `()` call captures one exact
    // instant, which won't match the real call's (different) instant. Use any() instead.
    every { getGreetingTextUseCase(any()) } returns "Good morning"
    every { googleTasksAuthManager.isAuthorized() } returns true
    coEvery { getActiveEventsForTheDayUseCase(any(), any()) } returns emptyList()
    every { getTimeSectionsUseCase(any()) } returns emptyList()
    coEvery { getNavigationItemsUseCase(any(), any()) } returns emptyList()
    every { legalDocumentRepository.hasUpdate(any()) } returns false
    every { prefs.isUserLogged } returns true
    every { featureFlags.isEnabled(any()) } returns false
    every { whatsNewManager.hasChanges() } returns false

    viewModel =
      ScheduleHomeViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        getActiveEventsForTheDayUseCase = getActiveEventsForTheDayUseCase,
        getTimeSectionsUseCase = getTimeSectionsUseCase,
        getGreetingTextUseCase = getGreetingTextUseCase,
        googleTasksAuthManager = googleTasksAuthManager,
        getNavigationItemsUseCase = getNavigationItemsUseCase,
        prefs = prefs,
        featureFlags = featureFlags,
        whatsNewManager = whatsNewManager,
        analyticsEventSender = analyticsEventSender,
        legalDocumentRepository = legalDocumentRepository,
      )
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
          ScheduleHomeViewModel.EventType.Reminder,
          ScheduleHomeViewModel.EventType.Birthday,
          ScheduleHomeViewModel.EventType.Note,
        ),
        state.addMenuItems,
      )
    }

  @Test
  fun `loadData shows an empty list state when there are no sections`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(ListState.Empty, state.listState)
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
      coEvery { getActiveEventsForTheDayUseCase(any(), any()) } returns listOf(event)
      every { getTimeSectionsUseCase(listOf(event)) } returns
        listOf(com.elementary.tasks.home.TimeSection(time = "9:00", event = event))

      val state = viewModel.state.first()

      val listState = state.listState
      assertTrue(listState is ListState.Ready)
      assertEquals(1, (listState as ListState.Ready).sections.size)
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
      every { prefs.isUserLogged } returns false
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

    verify { prefs.isUserLogged = true }
    assertEquals(null, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onLoginClick marks the user logged in and posts OpenCloudDrives`() {
    viewModel.onLoginClick()

    verify { prefs.isUserLogged = true }
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
}
