package com.github.naz013.repository.entity

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderWithGroupEntityTest {

  @Test
  fun `toDomain falls back to null title and zero color when there is no group`() {
    val entityWithGroup = ReminderWithGroupEntity(
      reminder = ReminderEntity(Reminder(groupUuId = "missing-group")),
      reminderGroup = null
    )

    val domain = entityWithGroup.toDomain()

    assertNull(domain.groupTitle)
    assertEquals(0, domain.groupColor)
  }

  @Test
  fun `toDomain carries the joined group's title and color`() {
    val group = ReminderGroupEntity(
      ReminderGroup(
        groupTitle = "Shopping",
        groupUuId = "group-1",
        groupColor = 5,
        groupDateTime = "",
        isDefaultGroup = false,
        syncState = SyncState.Synced
      )
    )
    val entityWithGroup = ReminderWithGroupEntity(
      reminder = ReminderEntity(Reminder(groupUuId = "group-1")),
      reminderGroup = group
    )

    val domain = entityWithGroup.toDomain()

    assertEquals("Shopping", domain.groupTitle)
    assertEquals(5, domain.groupColor)
  }
}
