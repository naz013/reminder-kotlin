package com.github.naz013.repository.impl

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.entity.ReminderWithGroupEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReminderRepositoryImplTest {
  private val dao = mockk<ReminderDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxed = true)
  private lateinit var repository: ReminderRepositoryImpl

  @Before
  fun setUp() {
    repository = ReminderRepositoryImpl(dao, notifier, reminderV2Repository)
  }

  @Test
  fun `getActiveWithoutGpsTypes filters out every gps type`() = runTest {
    val gpsReminder = ReminderWithGroupEntity(
      reminder = ReminderEntity(Reminder(uuId = "gps", type = Reminder.BY_LOCATION_CALL)),
      reminderGroup = null
    )
    val dateReminder = ReminderWithGroupEntity(
      reminder = ReminderEntity(Reminder(uuId = "date", type = Reminder.BY_DATE)),
      reminderGroup = null
    )
    every { dao.getAll(active = true, removed = false) } returns listOf(gpsReminder, dateReminder)

    val result = repository.getActiveWithoutGpsTypes()

    assertEquals(listOf("date"), result.map { it.uuId })
  }

  @Test
  fun `getActiveWithoutGpsTypes keeps every non-gps type`() = runTest {
    val weekReminder = ReminderWithGroupEntity(
      reminder = ReminderEntity(Reminder(uuId = "week", type = Reminder.BY_WEEK)),
      reminderGroup = null
    )
    every { dao.getAll(active = true, removed = false) } returns listOf(weekReminder)

    val result = repository.getActiveWithoutGpsTypes()

    assertEquals(listOf("week"), result.map { it.uuId })
  }

  @Test
  fun `save mirrors the reminder into ReminderV2Repository`() = runTest {
    val reminder = Reminder(uuId = "1", type = Reminder.BY_DATE, summary = "Test")

    repository.save(reminder)

    coVerify(exactly = 1) { reminderV2Repository.save(match { it.uuId == "1" }) }
  }

  @Test
  fun `delete mirrors into ReminderV2Repository`() = runTest {
    repository.delete("1")

    coVerify(exactly = 1) { reminderV2Repository.delete("1") }
  }

  @Test
  fun `deleteAll mirrors into ReminderV2Repository`() = runTest {
    repository.deleteAll()

    coVerify(exactly = 1) { reminderV2Repository.deleteAll() }
  }

  @Test
  fun `deleteAll by ids mirrors into ReminderV2Repository`() = runTest {
    repository.deleteAll(listOf("1", "2"))

    coVerify(exactly = 1) { reminderV2Repository.deleteAll(listOf("1", "2")) }
  }

  @Test
  fun `updateSyncState mirrors into ReminderV2Repository`() = runTest {
    repository.updateSyncState("1", SyncState.Synced)

    coVerify(exactly = 1) { reminderV2Repository.updateSyncState("1", SyncState.Synced) }
  }
}
