package com.github.naz013.repository.impl

import com.github.naz013.domain.Reminder
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.entity.ReminderWithGroupEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReminderRepositoryImplTest {
  private val dao = mockk<ReminderDao>()
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: ReminderRepositoryImpl

  @Before
  fun setUp() {
    repository = ReminderRepositoryImpl(dao, notifier)
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
}
