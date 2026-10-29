package com.github.naz013.repository.entity

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.BirthdayEventType
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test

class BirthdayEntityTest {

  @Test
  fun `round trips eventType and customLabel`() {
    val birthday = Birthday(
      name = "Alex",
      eventType = BirthdayEventType.MEMORIAL,
      customLabel = "Grandpa",
      syncState = SyncState.Synced
    )

    val result = BirthdayEntity(birthday).toDomain()

    assertEquals(birthday, result)
  }

  @Test
  fun `maps unknown eventType string to BIRTHDAY`() {
    val entity = BirthdayEntity(syncState = SyncState.Synced.name, eventType = "SOMETHING_NEW")

    assertEquals(BirthdayEventType.BIRTHDAY, entity.toDomain().eventType)
  }
}
