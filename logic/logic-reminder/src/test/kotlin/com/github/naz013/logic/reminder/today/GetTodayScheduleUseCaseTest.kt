package com.github.naz013.logic.reminder.today

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.CalendarEvent
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

class GetTodayScheduleUseCaseTest {

  private val now = LocalDateTime.of(2026, 10, 17, 12, 0)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val reminderRepository = mockk<ReminderV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val googleCalendarEventRepository = mockk<GoogleCalendarEventRepository>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val calendarEventRepository = mockk<CalendarEventRepository>()

  private val useCase = GetTodayScheduleUseCase(
    dateTimeManager,
    reminderRepository,
    birthdayRepository,
    googleCalendarEventRepository,
    googleTaskRepository,
    calendarEventRepository,
  )

  @Before
  fun setUp() {
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.fromMillis(any()) } answers {
      LocalDateTime.ofInstant(org.threeten.bp.Instant.ofEpochMilli(firstArg()), ZoneId.systemDefault())
    }
    every { dateTimeManager.getBirthdayLocalTime() } returns null
    every { dateTimeManager.getBirthdayDayMonth(any()) } returns "17|9"
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns emptyList()
    coEvery { birthdayRepository.getAll("17|9") } returns emptyList()
    coEvery { googleCalendarEventRepository.getVisible() } returns emptyList()
    coEvery { googleTaskRepository.getAll() } returns emptyList()
    coEvery { calendarEventRepository.getAll() } returns emptyList()
  }

  @Test
  fun `returns empty list when nothing is scheduled`() = runTest {
    val result = useCase(now)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `marks reminder from an earlier day as overdue`() = runTest {
    val reminder = reminder("r1", now.minusDays(2))
    val tomorrow = reminder("r2", now.plusDays(1))
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns listOf(reminder, tomorrow)

    val result = useCase(now)

    assertEquals(1, result.size)
    val item = result.single() as TodayItem.Reminder
    assertTrue(item.isOverdue)
    assertEquals(TodayBucket.Timed, item.bucket)
  }

  @Test
  fun `does not mark reminder due later today as overdue`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("r1", now.withHour(18)))

    val item = useCase(now).single() as TodayItem.Reminder

    assertFalse(item.isOverdue)
  }

  @Test
  fun `drops calendar event whose device event id matches reminder export`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("r1", now.withHour(15)))
    coEvery { googleCalendarEventRepository.getVisible() } returns
      listOf(calendarEvent(deviceEventId = 77, start = now.withHour(15), title = "Other title"))
    coEvery { calendarEventRepository.getAll() } returns
      listOf(CalendarEvent(reminderId = "r1", event = "x", eventId = 77, allDay = false))

    val result = useCase(now)

    val item = result.single() as TodayItem.Reminder
    assertTrue(item.alsoInCalendar)
  }

  @Test
  fun `drops calendar event by calendar, start and title when ids do not match`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("r1", now.withHour(15), summary = "Dentist", calendarId = 5))
    coEvery { googleCalendarEventRepository.getVisible() } returns
      listOf(calendarEvent(deviceEventId = 99, calendarId = 5, start = now.withHour(15), title = " dentist "))

    val result = useCase(now)

    val item = result.single() as TodayItem.Reminder
    assertTrue(item.alsoInCalendar)
  }

  @Test
  fun `keeps calendar event from another calendar with the same title and start`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("r1", now.withHour(15), summary = "Dentist", calendarId = 5))
    coEvery { googleCalendarEventRepository.getVisible() } returns
      listOf(calendarEvent(deviceEventId = 99, calendarId = 6, start = now.withHour(15), title = "Dentist"))

    val result = useCase(now)

    assertEquals(2, result.size)
    assertFalse((result.filterIsInstance<TodayItem.Reminder>().single()).alsoInCalendar)
    assertEquals(1, result.filterIsInstance<TodayItem.CalendarEvent>().size)
  }

  @Test
  fun `hides google task whose uuid equals a reminder uuid`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("r1", now.withHour(15)))
    coEvery { googleTaskRepository.getAll() } returns
      listOf(task(uuId = "r1", due = now.toLocalDate().atStartOfDay()))

    val result = useCase(now)

    val item = result.single() as TodayItem.Reminder
    assertTrue(item.alsoInTasks)
  }

  @Test
  fun `puts all day calendar event into the all day bucket`() = runTest {
    coEvery { googleCalendarEventRepository.getVisible() } returns
      listOf(calendarEvent(deviceEventId = 1, start = now.toLocalDate().atStartOfDay(), allDay = true))

    val item = useCase(now).single()

    assertEquals(TodayBucket.AllDay, item.bucket)
  }

  @Test
  fun `expands recurring calendar event to its occurrence today`() = runTest {
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } answers {
      firstArg<LocalDateTime>().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val series = calendarEvent(
      deviceEventId = 5,
      start = now.minusDays(3).withHour(9),
      title = "Standup",
    ).copy(rrule = "FREQ=DAILY;COUNT=10")
    coEvery { googleCalendarEventRepository.getVisible() } returns listOf(series)

    val result = useCase(now)

    val item = result.single() as TodayItem.CalendarEvent
    assertEquals(LocalDateTime.of(2026, 10, 17, 9, 0), item.dateTime)
  }

  @Test
  fun `falls back to base start when recurrence rule cannot be parsed`() = runTest {
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } answers {
      firstArg<LocalDateTime>().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val broken = calendarEvent(deviceEventId = 6, start = now.withHour(9)).copy(rrule = "FREQ=NOPE;;")
    coEvery { googleCalendarEventRepository.getVisible() } returns listOf(broken)

    val item = useCase(now).single() as TodayItem.CalendarEvent

    assertEquals(now.withHour(9), item.dateTime)
  }

  @Test
  fun `shows timed calendar event that started yesterday and ends today`() = runTest {
    val event = calendarEvent(deviceEventId = 7, start = now.minusDays(1).withHour(22))
      .copy(endDateTime = now.withHour(2))
    coEvery { googleCalendarEventRepository.getVisible() } returns listOf(event)

    val item = useCase(now).single() as TodayItem.CalendarEvent

    assertEquals(TodayBucket.Timed, item.bucket)
    assertEquals(now.withHour(2), item.endDateTime)
  }

  @Test
  fun `sorts overdue reminder before todays timed items`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns listOf(
      reminder("today", now.withHour(8)),
      reminder("old", now.minusDays(3).withHour(20)),
    )
    coEvery { googleCalendarEventRepository.getVisible() } returns
      listOf(calendarEvent(deviceEventId = 8, start = now.withHour(7)))

    val result = useCase(now)

    assertEquals(3, result.size)
    assertTrue(result.all { it.bucket == TodayBucket.Timed })
    assertEquals("old", result.first().id)
  }

  @Test
  fun `hides calendar event mirroring an active reminder that is due later`() = runTest {
    coEvery { reminderRepository.getAll(active = true, removed = false) } returns
      listOf(reminder("future", now.plusDays(2)))
    coEvery { googleCalendarEventRepository.getVisible() } returns
      listOf(calendarEvent(deviceEventId = 77, start = now.withHour(15)))
    coEvery { calendarEventRepository.getAll() } returns
      listOf(CalendarEvent(reminderId = "future", event = "x", eventId = 77, allDay = false))

    val result = useCase(now)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `puts midnight google task due today into the anytime bucket`() = runTest {
    coEvery { googleTaskRepository.getAll() } returns
      listOf(task(uuId = "t1", due = now.toLocalDate().atStartOfDay()))

    val item = useCase(now).single()

    assertEquals(TodayBucket.Anytime, item.bucket)
  }

  @Test
  fun `shows birthday that was not shown this year`() = runTest {
    coEvery { birthdayRepository.getAll("17|9") } returns listOf(Birthday(name = "Ann", showedYear = 2025, syncState = SyncState.Synced))

    val result = useCase(now)

    assertEquals(1, result.filterIsInstance<TodayItem.Birthday>().size)
  }

  @Test
  fun `excludes birthday already shown this year`() = runTest {
    coEvery { birthdayRepository.getAll("17|9") } returns listOf(Birthday(name = "Ann", showedYear = 2026, syncState = SyncState.Synced))

    val result = useCase(now)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `places birthday at configured time in the timed bucket`() = runTest {
    every { dateTimeManager.getBirthdayLocalTime() } returns LocalTime.of(9, 0)
    coEvery { birthdayRepository.getAll("17|9") } returns listOf(Birthday(name = "Ann", syncState = SyncState.Synced))

    val item = useCase(now).single()

    assertEquals(TodayBucket.Timed, item.bucket)
    assertEquals(LocalTime.of(9, 0), item.dateTime.toLocalTime())
  }

  private fun reminder(
    id: String,
    at: LocalDateTime,
    summary: String = "Reminder $id",
    calendarId: Long? = null,
  ) = ReminderV2(
    uuId = id,
    summary = summary,
    schedule = ReminderSchedule(startDateTime = at, eventDateTime = at),
    calendarExport = calendarId?.let { CalendarExportSettings(it, 0, false) },
  )

  private fun calendarEvent(
    deviceEventId: Long,
    start: LocalDateTime,
    calendarId: Long = 1,
    title: String = "Event",
    allDay: Boolean = false,
  ) = GoogleCalendarEvent(
    deviceEventId = deviceEventId,
    calendarId = calendarId,
    calendarName = "cal",
    title = title,
    description = "",
    startDateTime = start,
    endDateTime = null,
    allDay = allDay,
    rrule = "",
  )

  private fun task(
    uuId: String,
    due: LocalDateTime,
  ) = GoogleTask(
    title = "Task",
    taskId = "task-$uuId",
    uuId = uuId,
    status = GoogleTask.TASKS_NEED_ACTION,
    dueDate = due.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
  )
}
