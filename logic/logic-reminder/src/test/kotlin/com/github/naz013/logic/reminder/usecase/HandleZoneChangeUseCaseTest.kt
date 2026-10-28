package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.TimeZone
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class HandleZoneChangeUseCaseTest {
  private val repository = mockk<ReminderV2Repository>()
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val preferences = mockk<ReminderPreferences>(relaxed = true)
  private val originalZone = TimeZone.getDefault()

  private val useCase =
    HandleZoneChangeUseCase(
      reminderRepository = repository,
      saveReminderUseCase = saveReminderUseCase,
      dateTimeManager = DateTimeManager(mockk(relaxed = true), mockk(relaxed = true)),
      reminderPreferences = preferences,
    )

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalZone)
  }

  private fun reminder(
    id: String = "r",
    zone: String? = null,
    recurrence: RecurrenceRule = RecurrenceRule.Once,
    places: List<Place> = emptyList(),
    active: Boolean = true,
    eventDateTime: LocalDateTime? = EVENT_UTC,
  ) = ReminderV2(
    uuId = id,
    timeZoneId = zone,
    recurrence = recurrence,
    places = places,
    isActive = active,
    schedule = ReminderSchedule(startDateTime = EVENT_UTC, eventDateTime = eventDateTime),
  )

  private fun givenStoredZone(id: String?) {
    every { preferences.lastKnownZoneId } returns id
  }

  private fun givenActive(vararg reminders: ReminderV2) {
    coEvery { repository.getAll(active = true, removed = false) } returns reminders.filter { it.isActive }
  }

  @Test
  fun `does nothing when zone is unchanged`() = runTest {
    givenStoredZone("America/New_York")
    givenActive(reminder())

    val changed = useCase()

    assertFalse(changed)
    coVerify(exactly = 0) { saveReminderUseCase(any()) }
  }

  @Test
  fun `initialises last known zone without re-anchoring on first run`() = runTest {
    givenStoredZone(null)
    givenActive(reminder())

    val changed = useCase()

    assertFalse(changed)
    verify { preferences.lastKnownZoneId = "America/New_York" }
    coVerify(exactly = 0) { saveReminderUseCase(any()) }
  }

  @Test
  fun `re-anchors floating reminder keeping wall clock when zone changed`() = runTest {
    givenStoredZone("Europe/Berlin")
    givenActive(reminder())
    val saved = slot<ReminderV2>()

    val changed = useCase()

    assertTrue(changed)
    coVerify { saveReminderUseCase(capture(saved)) }
    assertEquals(LocalDateTime.of(2026, 6, 1, 13, 0), saved.captured.schedule.eventDateTime)
    assertEquals(1L, saved.captured.sync.version)
    assertEquals(SyncState.WaitingForUpload, saved.captured.sync.syncState)
    verify { preferences.lastKnownZoneId = "America/New_York" }
  }

  @Test
  fun `leaves fixed zone reminders untouched`() = runTest {
    givenStoredZone("Europe/Berlin")
    givenActive(reminder(zone = "Asia/Tokyo"))

    useCase()

    coVerify(exactly = 0) { saveReminderUseCase(any()) }
  }

  @Test
  fun `leaves countdown and location reminders untouched`() = runTest {
    givenStoredZone("Europe/Berlin")
    givenActive(
      reminder(id = "a", recurrence = RecurrenceRule.Countdown(after = 1000)),
      reminder(id = "b", recurrence = RecurrenceRule.LocationEnter),
      reminder(id = "c", places = listOf(Place(syncState = SyncState.Synced))),
    )

    useCase()

    coVerify(exactly = 0) { saveReminderUseCase(any()) }
  }

  @Test
  fun `leaves inactive and event-less reminders untouched`() = runTest {
    givenStoredZone("Europe/Berlin")
    givenActive(reminder(id = "a", active = false), reminder(id = "b", eventDateTime = null))

    useCase()

    coVerify(exactly = 0) { saveReminderUseCase(any()) }
  }

  @Test
  fun `saves a re-anchored time that is already in the past for activation to handle`() = runTest {
    val past = LocalDateTime.of(2020, 1, 1, 7, 0)
    givenStoredZone("Europe/Berlin")
    givenActive(reminder(eventDateTime = past))
    val saved = slot<ReminderV2>()

    useCase()

    coVerify { saveReminderUseCase(capture(saved)) }
    assertEquals(LocalDateTime.of(2020, 1, 1, 13, 0), saved.captured.schedule.eventDateTime)
  }

  @Test
  fun `keeps start date time unchanged when re-anchoring`() = runTest {
    givenStoredZone("Europe/Berlin")
    givenActive(reminder())
    val saved = slot<ReminderV2>()

    useCase()

    coVerify { saveReminderUseCase(capture(saved)) }
    assertEquals(EVENT_UTC, saved.captured.schedule.startDateTime)
  }

  @Test
  fun `retries only the failed reminder and never shifts a succeeded one twice`() = runTest {
    var stored: String? = "Europe/Berlin"
    var pending: String? = null
    every { preferences.lastKnownZoneId } answers { stored }
    every { preferences.lastKnownZoneId = any() } answers { stored = firstArg() }
    every { preferences.pendingZoneReanchor } answers { pending }
    every { preferences.pendingZoneReanchor = any() } answers { pending = firstArg() }
    val ok = reminder(id = "ok")
    val bad = reminder(id = "bad")
    givenActive(ok, bad)
    coEvery { saveReminderUseCase(match { it.uuId == "bad" }) } throws IllegalStateException("db")

    useCase()

    assertEquals("America/New_York", stored)
    assertEquals("Europe/Berlin=bad", pending)

    val saved = mutableListOf<ReminderV2>()
    coEvery { saveReminderUseCase(capture(saved)) } returns Unit

    val retried = useCase()

    assertTrue(retried)
    assertEquals(listOf("bad"), saved.map { it.uuId })
    assertEquals(LocalDateTime.of(2026, 6, 1, 13, 0), saved.single().schedule.eventDateTime)
    assertEquals(null, pending)
  }

  companion object {
    private val EVENT_UTC = LocalDateTime.of(2026, 6, 1, 7, 0)
  }
}
