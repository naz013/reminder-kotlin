package com.elementary.tasks.core.services.usecase

import android.content.Context
import android.location.Location
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.reminder.usecase.StopLocationTrackingUseCase
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class CheckLocationReminderUseCaseTest : BaseTest() {

  private val context = mockk<Context>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val workflowTriggerRunner = mockk<WorkflowTriggerRunner>(relaxed = true)
  private val stopLocationTrackingUseCase = mockk<StopLocationTrackingUseCase>(relaxed = true)
  private val placeDistanceCalculator = mockk<PlaceDistanceCalculator>()
  private val prefs = mockk<Prefs>(relaxed = true)

  private val testLocation = Location("point A")

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.useMetric } returns true
    every { prefs.radius } returns 50
    every { prefs.isDistanceNotificationEnabled } returns false
    coEvery { reminderV2Repository.save(any()) } just Runs
  }

  private fun buildUseCase() =
    CheckLocationReminderUseCase(
      context = context,
      reminderV2Repository = reminderV2Repository,
      dateTimeManager = dateTimeManager,
      workflowTriggerRunner = workflowTriggerRunner,
      prefs = prefs,
      stopLocationTrackingUseCase = stopLocationTrackingUseCase,
      placeDistanceCalculator = placeDistanceCalculator,
    )

  private fun place(radius: Int = 50) =
    Place(radius = radius, latitude = 1.0, longitude = 1.0, syncState = SyncState.Synced)

  private fun reminder(
    recurrence: RecurrenceRule,
    places: List<Place> = listOf(place()),
    location: LocationSettings? = LocationSettings(),
    eventDateTime: LocalDateTime? = null,
  ) = ReminderV2(
    recurrence = recurrence,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = eventDateTime),
    location = location,
    places = places,
  )

  private fun stubDistance(meters: Int) {
    every { placeDistanceCalculator.metersTo(any(), any()) } returns meters
  }

  private fun stubReminders(vararg reminders: ReminderV2) {
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns reminders.toList()
  }

  @Test
  fun `arriving reminder fires when within radius`() = runTest {
    stubDistance(30)
    val reminder = reminder(RecurrenceRule.LocationEnter)
    stubReminders(reminder)

    val result = buildUseCase().invoke(testLocation)

    assertEquals(1, result.showReminderNotifications.size)
    assertEquals(reminder.uuId, result.showReminderNotifications[0].uuId)
  }

  @Test
  fun `arriving reminder does not fire when outside radius`() = runTest {
    stubDistance(100)
    stubReminders(reminder(RecurrenceRule.LocationEnter))

    val result = buildUseCase().invoke(testLocation)

    assertTrue(result.showReminderNotifications.isEmpty())
  }

  @Test
  fun `arriving reminder falls back to the stock radius when place radius is unset`() = runTest {
    every { prefs.radius } returns 200
    stubDistance(150)
    stubReminders(reminder(RecurrenceRule.LocationEnter, places = listOf(place(radius = -1))))

    val result = buildUseCase().invoke(testLocation)

    assertEquals(1, result.showReminderNotifications.size)
  }

  @Test
  fun `leaving reminder does not fire while outside radius and never locked`() = runTest {
    stubDistance(100)
    stubReminders(reminder(RecurrenceRule.LocationExit, location = LocationSettings(isLocked = false)))

    val result = buildUseCase().invoke(testLocation)

    assertTrue(result.showReminderNotifications.isEmpty())
    coVerify(exactly = 0) { reminderV2Repository.save(any()) }
  }

  @Test
  fun `leaving reminder locks once observed within radius`() = runTest {
    stubDistance(20)
    stubReminders(reminder(RecurrenceRule.LocationExit, location = LocationSettings(isLocked = false)))
    val saved = slot<ReminderV2>()
    coEvery { reminderV2Repository.save(capture(saved)) } just Runs

    val result = buildUseCase().invoke(testLocation)

    assertTrue(result.showReminderNotifications.isEmpty())
    assertEquals(true, saved.captured.location?.isLocked)
  }

  @Test
  fun `leaving reminder fires once locked and then moving outside radius`() = runTest {
    stubDistance(200)
    val reminder = reminder(RecurrenceRule.LocationExit, location = LocationSettings(isLocked = true))
    stubReminders(reminder)

    val result = buildUseCase().invoke(testLocation)

    assertEquals(1, result.showReminderNotifications.size)
    coVerify { workflowTriggerRunner.onLocationExited(reminder.uuId) }
  }

  @Test
  fun `already fired reminder is skipped on subsequent checks`() = runTest {
    stubDistance(10)
    stubReminders(reminder(RecurrenceRule.LocationEnter, location = LocationSettings(isNotificationShown = true)))

    val result = buildUseCase().invoke(testLocation)

    assertTrue(result.showReminderNotifications.isEmpty())
    coVerify(exactly = 0) { reminderV2Repository.save(any()) }
  }

  @Test
  fun `firing a reminder checks whether location tracking can be stopped`() = runTest {
    stubDistance(10)
    stubReminders(reminder(RecurrenceRule.LocationEnter))

    val result = buildUseCase().invoke(testLocation)

    assertEquals(1, result.showReminderNotifications.size)
    coVerify { stopLocationTrackingUseCase(any(), false) }
  }

  @Test
  fun `distance notification is shown for arriving reminder when enabled`() = runTest {
    every { prefs.isDistanceNotificationEnabled } returns true
    stubDistance(500)
    stubReminders(reminder(RecurrenceRule.LocationEnter))

    val result = buildUseCase().invoke(testLocation)

    assertEquals(1, result.showDistanceNotifications.size)
    assertTrue(result.showReminderNotifications.isEmpty())
  }

  @Test
  fun `distance notification is not shown when disabled in preferences`() = runTest {
    every { prefs.isDistanceNotificationEnabled } returns false
    stubDistance(500)
    stubReminders(reminder(RecurrenceRule.LocationEnter))

    val result = buildUseCase().invoke(testLocation)

    assertTrue(result.showDistanceNotifications.isEmpty())
  }

  @Test
  fun `distance is not checked while a delayed reminder's start time is still upcoming`() = runTest {
    stubDistance(10)
    val futureDelay = LocalDateTime.now().plusHours(1)
    every { dateTimeManager.utcToLocal(futureDelay) } returns futureDelay
    every { dateTimeManager.isCurrent(futureDelay) } returns true
    stubReminders(reminder(RecurrenceRule.LocationEnter, eventDateTime = futureDelay))

    val result = buildUseCase().invoke(testLocation)

    assertTrue(result.showReminderNotifications.isEmpty())
  }

  @Test
  fun `distance is checked once a delayed reminder's start time has passed`() = runTest {
    stubDistance(10)
    val pastDelay = LocalDateTime.now().minusMinutes(1)
    every { dateTimeManager.utcToLocal(pastDelay) } returns pastDelay
    every { dateTimeManager.isCurrent(pastDelay) } returns false
    stubReminders(reminder(RecurrenceRule.LocationEnter, eventDateTime = pastDelay))

    val result = buildUseCase().invoke(testLocation)

    assertEquals(1, result.showReminderNotifications.size)
  }
}
