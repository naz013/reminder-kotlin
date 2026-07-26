package com.github.naz013.repository.impl

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.dao.ReminderV2Dao
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderV2RepositoryImplTest {
  private val dao = mockk<ReminderV2Dao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private val reminderDao = mockk<ReminderDao>(relaxed = true)
  private lateinit var repository: ReminderV2RepositoryImpl

  @Before
  fun setUp() {
    repository = ReminderV2RepositoryImpl(dao, notifier, reminderDao)
  }

  private fun reminderV2(uuId: String = "1") =
    ReminderV2(
      uuId = uuId,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  @Test
  fun `save mirrors the reminder into the V1 ReminderDao`() = runTest {
    repository.save(reminderV2("1"))

    coVerify(exactly = 1) { reminderDao.insert(match { it.uuId == "1" }) }
  }

  @Test
  fun `save forces jsonSchemaVersion to V3 on the mirrored V1 copy`() = runTest {
    val captured = slot<ReminderEntity>()

    repository.save(reminderV2("1"))

    coVerify(exactly = 1) { reminderDao.insert(capture(captured)) }
    assertEquals(Reminder.Version.V3, captured.captured.version)
  }

  @Test
  fun `delete mirrors into the V1 ReminderDao`() = runTest {
    repository.delete("1")

    coVerify(exactly = 1) { reminderDao.delete("1") }
  }

  @Test
  fun `deleteAll mirrors into the V1 ReminderDao`() = runTest {
    repository.deleteAll()

    coVerify(exactly = 1) { reminderDao.deleteAll() }
  }

  @Test
  fun `deleteAll by ids mirrors into the V1 ReminderDao`() = runTest {
    repository.deleteAll(listOf("1", "2"))

    coVerify(exactly = 1) { reminderDao.deleteAll(listOf("1", "2")) }
  }

  @Test
  fun `updateSyncState mirrors into the V1 ReminderDao`() = runTest {
    repository.updateSyncState("1", SyncState.Synced)

    coVerify(exactly = 1) { reminderDao.updateSyncState("1", SyncState.Synced.name) }
  }
}
