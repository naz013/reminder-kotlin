package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.RoutineRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class SaveRoutineUseCaseTest {
  private val routineRepository = mockk<RoutineRepository>(relaxed = true)
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private lateinit var useCase: SaveRoutineUseCase

  private val now = LocalDateTime.of(2026, 7, 22, 9, 0)

  @Before
  fun setUp() {
    every { nowDateTimeProvider.nowDateTime() } returns now
    useCase = SaveRoutineUseCase(routineRepository, nowDateTimeProvider, scheduleBackgroundWorkUseCase)
  }

  @Test
  fun `invoke bumps version, marks WaitingForUpload, and stamps updatedAt`() = runTest {
    val routine = Routine(
      id = "id-1",
      title = "Morning routine",
      createdAt = LocalDateTime.of(2026, 7, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 7, 1, 8, 0),
      sync = SyncMetadata(version = 2L, syncState = SyncState.Synced)
    )

    val result = useCase(routine)

    assertEquals(now, result.updatedAt)
    assertEquals(3L, result.sync.version)
    assertEquals(SyncState.WaitingForUpload, result.sync.syncState)
    coVerify(exactly = 1) { routineRepository.save(result) }
  }

  @Test
  fun `invoke schedules an upload for the routine's DataType`() = runTest {
    val routine = Routine(
      id = "id-2",
      title = "Evening routine",
      createdAt = now,
      updatedAt = now
    )

    useCase(routine)

    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Upload,
        dataType = DataType.Routines,
        id = "id-2"
      )
    }
  }
}
