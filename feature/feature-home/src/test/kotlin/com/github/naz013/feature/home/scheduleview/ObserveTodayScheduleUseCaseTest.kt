package com.github.naz013.feature.home.scheduleview

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.today.GetTodayScheduleUseCase
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.testing.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveTodayScheduleUseCaseTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val getTodayScheduleUseCase = mockk<GetTodayScheduleUseCase>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val reminderChanges = MutableSharedFlow<List<ReminderV2>>()

  private val now = LocalDateTime.of(2026, 7, 15, 12, 0)
  private lateinit var useCase: ObserveTodayScheduleUseCase

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { reminderV2Repository.observeByRemovedStatus(false) } returns reminderChanges
    every { birthdayRepository.observeAll() } returns emptyFlow()
    every { googleTaskRepository.observeAll() } returns emptyFlow()
    useCase = ObserveTodayScheduleUseCase(
      dateTimeManager,
      getTodayScheduleUseCase,
      reminderV2Repository,
      birthdayRepository,
      googleTaskRepository,
    )
  }

  private fun item(bucket: TodayBucket) = TodayItem.Birthday(Birthday(uuId = "b", name = "b", syncState = SyncState.Synced), now, bucket)

  @Test
  fun `re-queries every minute while timed items exist`() = runTest {
    coEvery { getTodayScheduleUseCase(any()) } returns listOf(item(TodayBucket.Timed))
    every { dateTimeManager.getCurrentDateTime() } answers {
      now.plus(testScheduler.currentTime, ChronoUnit.MILLIS)
    }
    val emissions = mutableListOf<TodaySnapshot>()

    val job = useCase().onEach { emissions.add(it) }.launchIn(this)
    runCurrent()
    advanceTimeBy(ObserveTodayScheduleUseCase.TICK_MILLIS * 2 + 1)
    job.cancel()

    assertEquals(3, emissions.size)
    assertEquals(now.plusMinutes(2), emissions.last().now)
  }

  @Test
  fun `does not tick when there are no timed items`() = runTest {
    coEvery { getTodayScheduleUseCase(now) } returns listOf(item(TodayBucket.AllDay))

    val job = useCase().onEach { }.launchIn(this)
    runCurrent()
    advanceTimeBy(ObserveTodayScheduleUseCase.TICK_MILLIS * 3)
    job.cancel()

    coVerify(exactly = 1) { getTodayScheduleUseCase(now) }
  }

  @Test
  fun `re-queries when a repository emits a change`() = runTest {
    coEvery { getTodayScheduleUseCase(now) } returns listOf(item(TodayBucket.AllDay))

    val job = useCase().onEach { }.launchIn(this)
    runCurrent()
    reminderChanges.emit(emptyList())
    runCurrent()
    job.cancel()

    coVerify(exactly = 2) { getTodayScheduleUseCase(now) }
  }
}
