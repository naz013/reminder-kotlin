package com.github.naz013.sync.local

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.HabitRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import org.threeten.bp.LocalDateTime

class HabitRepositoryCallerTest {
  private val repository = mockk<HabitRepository>(relaxed = true)
  private val caller = HabitRepositoryCaller(repository)

  @Test
  fun `insertOrUpdate saves the habit marked as synced`() = runTest {
    val habit = Habit(
      id = "h-1",
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      sync = SyncMetadata(version = 3L, syncState = SyncState.WaitingForUpload)
    )

    caller.insertOrUpdate(habit)

    coVerify {
      repository.save(habit.copy(sync = SyncMetadata(version = 3L, syncState = SyncState.Synced)))
    }
  }

  @Test
  fun `insertOrUpdate rejects items that are not habits`() = runTest {
    assertThrows(IllegalArgumentException::class.java) {
      kotlinx.coroutines.runBlocking { caller.insertOrUpdate("nope") }
    }
  }
}
