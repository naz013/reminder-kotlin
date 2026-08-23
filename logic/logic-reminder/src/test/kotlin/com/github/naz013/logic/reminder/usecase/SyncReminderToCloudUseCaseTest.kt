package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class SyncReminderToCloudUseCaseTest {
  private lateinit var saveReminderUseCase: SaveReminderUseCase

  private lateinit var useCase: SyncReminderToCloudUseCase

  @Before
  fun setUp() {
    saveReminderUseCase = mockk(relaxed = true)
    useCase = SyncReminderToCloudUseCase(saveReminderUseCase)
  }

  @Test
  fun `clears offlineOnly and forces sync state back to WaitingForUpload`() = runTest {
    val reminder = ReminderV2(
      uuId = "id-1",
      summary = "Test",
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      offlineOnly = true,
      sync = SyncMetadata(version = 3L, syncState = SyncState.Synced),
    )

    useCase(reminder)

    coVerify(exactly = 1) {
      saveReminderUseCase(
        match {
          it.uuId == "id-1" && !it.offlineOnly && it.sync.syncState == SyncState.WaitingForUpload
        },
      )
    }
  }
}
