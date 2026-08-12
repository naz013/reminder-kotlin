package com.github.naz013.logic.reminder.query

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ResolveReminderV2NotificationSettingsUseCaseTest {

  private val defaults = NotificationSettings(priority = ReminderPriority.NORMAL, vibrate = false, color = 1)
  private val settingsRepository = FakeReminderSettingsRepository(defaults)

  @Test
  fun `reminder override wins over group and settings`() = runTest {
    val group = GroupV2(uuId = "group-1", notification = NotificationSettingsOverride(priority = ReminderPriority.LOW))
    val groupRepository = FakeGroupV2Repository(mapOf(group.uuId to group))
    val useCase = ResolveReminderV2NotificationSettingsUseCase(groupRepository, settingsRepository)
    val reminder = reminderWith(
      groupId = "group-1",
      override = NotificationSettingsOverride(priority = ReminderPriority.HIGH)
    )

    val result = useCase(reminder)

    assertEquals(ReminderPriority.HIGH, result.priority)
  }

  @Test
  fun `group override wins when reminder does not set the field`() = runTest {
    val group = GroupV2(uuId = "group-1", notification = NotificationSettingsOverride(priority = ReminderPriority.LOW))
    val groupRepository = FakeGroupV2Repository(mapOf(group.uuId to group))
    val useCase = ResolveReminderV2NotificationSettingsUseCase(groupRepository, settingsRepository)
    val reminder = reminderWith(groupId = "group-1", override = NotificationSettingsOverride())

    val result = useCase(reminder)

    assertEquals(ReminderPriority.LOW, result.priority)
  }

  @Test
  fun `settings default wins when neither reminder nor group set the field`() = runTest {
    val group = GroupV2(uuId = "group-1", notification = NotificationSettingsOverride())
    val groupRepository = FakeGroupV2Repository(mapOf(group.uuId to group))
    val useCase = ResolveReminderV2NotificationSettingsUseCase(groupRepository, settingsRepository)
    val reminder = reminderWith(groupId = "group-1", override = NotificationSettingsOverride())

    val result = useCase(reminder)

    assertEquals(ReminderPriority.NORMAL, result.priority)
  }

  @Test
  fun `settings default wins when reminder has no group`() = runTest {
    val groupRepository = FakeGroupV2Repository(emptyMap())
    val useCase = ResolveReminderV2NotificationSettingsUseCase(groupRepository, settingsRepository)
    val reminder = reminderWith(groupId = null, override = NotificationSettingsOverride())

    val result = useCase(reminder)

    assertEquals(ReminderPriority.NORMAL, result.priority)
  }

  private fun reminderWith(groupId: String?, override: NotificationSettingsOverride) = ReminderV2(
    uuId = "reminder-1",
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0)),
    notification = override
  )
}

private class FakeGroupV2Repository(private val groups: Map<String, GroupV2>) : GroupV2Repository {
  override suspend fun save(group: GroupV2) = Unit
  override suspend fun saveAll(groups: List<GroupV2>) = Unit
  override suspend fun getAll(): List<GroupV2> = groups.values.toList()
  override suspend fun getById(id: String): GroupV2? = groups[id]
  override suspend fun defaultGroup(isDef: Boolean): GroupV2? = groups.values.firstOrNull { it.isDefault == isDef }
  override suspend fun search(query: String): List<GroupV2> = emptyList()
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = groups.keys.toList()
  override suspend fun setDefaultGroup(id: String, isDef: Boolean) = Unit
  override suspend fun countAll(): Int = groups.size
}

private class FakeReminderSettingsRepository(
  private var defaults: NotificationSettings
) : ReminderSettingsRepository {
  override fun getNotificationDefaults(): NotificationSettings = defaults
  override fun setNotificationDefaults(settings: NotificationSettings) {
    defaults = settings
  }
}
