package com.github.naz013.domain.reminder.migration

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

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

  @Test
  fun `toReminderGroup maps every field back to the V1 shape`() {
    val groupV2 = GroupV2(
      uuId = "group-1",
      title = "Work",
      color = 3,
      isDefault = true,
      createdAt = LocalDateTime.of(2026, 7, 22, 9, 0),
      version = 2L,
      syncState = SyncState.Synced
    )

    val result = groupV2.toReminderGroup()

    assertEquals("group-1", result.groupUuId)
    assertEquals("Work", result.groupTitle)
    assertEquals(3, result.groupColor)
    assertEquals(true, result.isDefaultGroup)
    assertEquals(2L, result.version)
    assertEquals(SyncState.Synced, result.syncState)
    assertEquals("2026-07-22 09:00:00.000+0000", result.groupDateTime)
  }
}
