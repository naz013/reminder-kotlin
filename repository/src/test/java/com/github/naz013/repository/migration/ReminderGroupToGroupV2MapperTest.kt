package com.github.naz013.repository.migration

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderGroupToGroupV2MapperTest {

  @Test
  fun `preserves the group id so ReminderV2 groupId references still resolve`() {
    val reminderGroup = ReminderGroup(
      groupTitle = "Work",
      groupUuId = "group-1",
      groupColor = 3,
      groupDateTime = "2026-07-22 09:00:00.000+0000",
      isDefaultGroup = true,
      version = 2L,
      syncState = SyncState.Synced
    )

    val result = reminderGroup.toGroupV2()

    assertEquals("group-1", result.uuId)
    assertEquals("Work", result.title)
    assertEquals(3, result.color)
    assertEquals(true, result.isDefault)
    assertEquals(2L, result.version)
    assertEquals(SyncState.Synced, result.syncState)
  }
}
