package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GroupV2MapperTest {

  @Test
  fun `toEntity then toDomain round trips a group with no overrides`() {
    val group = GroupV2(
      uuId = "group-1",
      title = "Work",
      color = 3,
      isDefault = true,
      createdAt = LocalDateTime.of(2026, 1, 1, 0, 0)
    )

    val roundTripped = group.toEntity().toDomain()

    assertEquals(group, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a group with notification overrides set`() {
    val group = GroupV2(
      uuId = "group-2",
      title = "Health",
      color = 5,
      isDefault = false,
      notification = NotificationSettingsOverride(
        priority = ReminderPriority.HIGH,
        vibrate = true,
        vibrationPattern = listOf(0L, 100L),
        bypassDoNotDisturb = true
      ),
      createdAt = LocalDateTime.of(2026, 2, 14, 12, 30),
      version = 2L,
      syncState = SyncState.Synced
    )

    val roundTripped = group.toEntity().toDomain()

    assertEquals(group, roundTripped)
  }
}
