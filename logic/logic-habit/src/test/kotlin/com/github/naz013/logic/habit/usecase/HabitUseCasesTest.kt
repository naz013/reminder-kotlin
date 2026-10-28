package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.files.DataType
import com.github.naz013.logic.habit.HabitStreakCalculator
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.nudge.ScheduleHabitNudgeUseCase
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitUseCasesTest {
  private val today = LocalDate.of(2026, 10, 18)
  private val now = LocalDateTime.of(2026, 10, 18, 9, 0)
  private val habitRepository = mockk<HabitRepository>(relaxed = true)
  private val checkInRepository = mockk<HabitCheckInRepository>(relaxed = true)
  private val schedule = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)
  private val scheduleNudge = mockk<ScheduleHabitNudgeUseCase>(relaxed = true)
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val nowProvider = mockk<NowDateTimeProvider> {
    every { nowDate() } returns today
    every { nowDateTime() } returns now
  }

  private fun habit(target: Int = 1) = Habit(
    id = "h-1",
    targetCount = target,
    createdAt = now,
    updatedAt = now
  )

  private fun checkIn(count: Int) = HabitCheckIn(
    habitId = "h-1",
    date = today,
    count = count,
    createdAt = now.minusDays(1),
    updatedAt = now.minusDays(1)
  )

  @Test
  fun `increment creates a check-in and completes it when the target is reached`() = runTest {
    coEvery { habitRepository.getById("h-1") } returns habit(target = 1)
    coEvery { checkInRepository.get("h-1", today) } returns null

    IncrementHabitCheckInUseCase(habitRepository, checkInRepository, nowProvider)("h-1")

    coVerify {
      checkInRepository.save(
        HabitCheckIn(habitId = "h-1", date = today, count = 1, isCompleted = true, createdAt = now, updatedAt = now)
      )
    }
  }

  @Test
  fun `increment keeps the check-in incomplete below target and preserves createdAt`() = runTest {
    coEvery { habitRepository.getById("h-1") } returns habit(target = 3)
    coEvery { checkInRepository.get("h-1", today) } returns checkIn(1)

    IncrementHabitCheckInUseCase(habitRepository, checkInRepository, nowProvider)("h-1")

    coVerify {
      checkInRepository.save(
        HabitCheckIn(
          habitId = "h-1",
          date = today,
          count = 2,
          isCompleted = false,
          createdAt = now.minusDays(1),
          updatedAt = now
        )
      )
    }
  }

  @Test
  fun `increment does nothing for an unknown habit`() = runTest {
    coEvery { habitRepository.getById("missing") } returns null

    IncrementHabitCheckInUseCase(habitRepository, checkInRepository, nowProvider)("missing")

    coVerify(exactly = 0) { checkInRepository.save(any()) }
  }

  @Test
  fun `toggle completes a day with no progress at the target count`() = runTest {
    coEvery { habitRepository.getById("h-1") } returns habit(target = 3)
    coEvery { checkInRepository.get("h-1", today) } returns null

    ToggleHabitCheckInUseCase(habitRepository, checkInRepository, nowProvider)("h-1")

    coVerify {
      checkInRepository.save(
        HabitCheckIn(habitId = "h-1", date = today, count = 3, isCompleted = true, createdAt = now, updatedAt = now)
      )
    }
  }

  @Test
  fun `toggle clears a day that already has progress`() = runTest {
    coEvery { habitRepository.getById("h-1") } returns habit()
    coEvery { checkInRepository.get("h-1", today) } returns checkIn(1)

    ToggleHabitCheckInUseCase(habitRepository, checkInRepository, nowProvider)("h-1")

    coVerify { checkInRepository.delete("h-1", today) }
    coVerify(exactly = 0) { checkInRepository.save(any()) }
  }

  @Test
  fun `save bumps version, marks waiting for upload and schedules an upload`() = runTest {
    val result = SaveHabitUseCase(habitRepository, nowProvider, schedule, scheduleNudge)(habit())

    assertEquals(1L, result.sync.version)
    assertEquals(SyncState.WaitingForUpload, result.sync.syncState)
    assertEquals(now, result.updatedAt)
    coVerify { habitRepository.save(result) }
    coVerify { scheduleNudge(result) }
    coVerify { schedule(workType = WorkType.Upload, dataType = DataType.Habits, id = "h-1") }
  }

  @Test
  fun `delete removes the habit and schedules a delete upload`() = runTest {
    DeleteHabitUseCase(habitRepository, schedule, jobScheduler)("h-1")

    coVerify { habitRepository.delete("h-1") }
    coVerify { jobScheduler.cancelHabitNudge("h-1") }
    coVerify { schedule(workType = WorkType.Delete, dataType = DataType.Habits, id = "h-1") }
  }

  @Test
  fun `observe combines each habit with today's count and streak`() = runTest {
    val daily = habit(target = 1)
    every { habitRepository.observeAll() } returns flowOf(listOf(daily))
    every { checkInRepository.observeRange("h-1", any(), any()) } returns flowOf(
      listOf(checkIn(1).copy(date = today), checkIn(1).copy(date = today.minusDays(1)))
    )

    val result = ObserveHabitsWithProgressUseCase(
      habitRepository,
      checkInRepository,
      HabitStreakCalculator(),
      nowProvider,
      mockDispatcherProvider()
    )().first()

    assertEquals(1, result.size)
    assertEquals(1, result[0].todayCount)
    assertEquals(2, result[0].streak)
  }

  @Test
  fun `observe re-reads progress for the new day after midnight`() = runTest {
    var date = LocalDate.of(2026, 10, 18)
    var time = LocalDateTime.of(2026, 10, 18, 23, 59, 59)
    val movingClock = mockk<NowDateTimeProvider> {
      every { nowDate() } answers { date }
      every { nowDateTime() } answers { time }
    }
    val dispatcher = StandardTestDispatcher(testScheduler)
    val dispatchers = mockk<DispatcherProvider> { every { io() } returns dispatcher }
    every { habitRepository.observeAll() } returns flowOf(listOf(habit()))
    val requestedTo = mutableListOf<LocalDate>()
    every { checkInRepository.observeRange("h-1", any(), any()) } answers {
      requestedTo += thirdArg<LocalDate>()
      flowOf(emptyList())
    }
    val results = mutableListOf<List<HabitWithProgress>>()
    backgroundScope.launch(dispatcher) {
      ObserveHabitsWithProgressUseCase(
        habitRepository,
        checkInRepository,
        HabitStreakCalculator(),
        movingClock,
        dispatchers
      )().collect { results += it }
    }
    runCurrent()
    assertEquals(listOf(LocalDate.of(2026, 10, 18)), requestedTo)

    date = LocalDate.of(2026, 10, 19)
    time = LocalDateTime.of(2026, 10, 19, 0, 0, 0)
    advanceTimeBy(1_100)
    runCurrent()

    assertEquals(listOf(LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 19)), requestedTo)
    assertEquals(2, results.size)
  }

  @Test
  fun `observe emits an empty list when there are no habits`() = runTest {
    every { habitRepository.observeAll() } returns flowOf(emptyList())

    val result = ObserveHabitsWithProgressUseCase(
      habitRepository,
      checkInRepository,
      HabitStreakCalculator(),
      nowProvider,
      mockDispatcherProvider()
    )().first()

    assertEquals(emptyList<Any>(), result)
  }
}
