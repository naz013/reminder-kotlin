package com.github.naz013.repository.entity

import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderEntityTest {

  @Test
  fun `constructor forces every place syncState to Synced regardless of the reminder's own state`() {
    val reminder = Reminder(
      places = listOf(
        Place(name = "Home", syncState = SyncState.WaitingForUpload),
        Place(name = "Work", syncState = SyncState.FailedToUpload)
      )
    )

    val entity = ReminderEntity(reminder)

    assertEquals(listOf(SyncState.Synced.name, SyncState.Synced.name), entity.places.map { it.syncState })
  }

  @Test
  fun `constructor stores the reminder syncState by name`() {
    val reminder = Reminder(syncState = SyncState.WaitingForUpload)

    val entity = ReminderEntity(reminder)

    assertEquals("WaitingForUpload", entity.syncState)
  }

  @Test
  fun `toDomain restores the syncState from its stored name`() {
    val entity = ReminderEntity(Reminder()).copy(syncState = SyncState.FailedToUpload.name)

    val domain = entity.toDomain()

    assertEquals(SyncState.FailedToUpload, domain.syncState)
  }

  @Test
  fun `round trip through entity preserves core fields`() {
    val reminder = Reminder(
      summary = "Buy milk",
      uuId = "uuid-1",
      eventTime = "2023-06-17 09:00:00",
      priority = 1,
      isActive = true
    )

    val restored = ReminderEntity(reminder).toDomain()

    assertEquals(reminder.summary, restored.summary)
    assertEquals(reminder.uuId, restored.uuId)
    assertEquals(reminder.eventTime, restored.eventTime)
    assertEquals(reminder.priority, restored.priority)
    assertEquals(reminder.isActive, restored.isActive)
  }
}
