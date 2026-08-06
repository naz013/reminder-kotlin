package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GetActiveRemindersV2ByGroupIdUseCaseTest {

  @Test
  fun `returns only active and not removed reminders for the group`() = runTest {
    val active = reminderWith(id = "active", groupId = "group-1", isActive = true, isRemoved = false)
    val inactive = reminderWith(id = "inactive", groupId = "group-1", isActive = false, isRemoved = false)
    val removed = reminderWith(id = "removed", groupId = "group-1", isActive = true, isRemoved = true)
    val repository = FakeGroupReminderV2Repository(mapOf("group-1" to listOf(active, inactive, removed)))
    val useCase = GetActiveRemindersV2ByGroupIdUseCase(repository)

    val result = useCase("group-1")

    assertEquals(listOf(active), result)
  }

  @Test
  fun `returns empty list when group has no reminders`() = runTest {
    val repository = FakeGroupReminderV2Repository(emptyMap())
    val useCase = GetActiveRemindersV2ByGroupIdUseCase(repository)

    val result = useCase("group-1")

    assertEquals(emptyList<ReminderV2>(), result)
  }

  private fun reminderWith(
    id: String,
    groupId: String?,
    isActive: Boolean,
    isRemoved: Boolean
  ) = ReminderV2(
    uuId = id,
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0)),
    isActive = isActive,
    isRemoved = isRemoved
  )
}

private class FakeGroupReminderV2Repository(
  private val byGroupId: Map<String, List<ReminderV2>>
) : ReminderV2Repository {
  override suspend fun save(reminder: ReminderV2) = Unit
  override suspend fun getById(id: String): ReminderV2? = null
  override suspend fun getAll(): List<ReminderV2> = emptyList()
  override suspend fun getAll(active: Boolean, removed: Boolean): List<ReminderV2> = emptyList()
  override suspend fun getByRemovedStatus(removed: Boolean): List<ReminderV2> = emptyList()
  override suspend fun getActiveInRange(
    removed: Boolean,
    from: LocalDateTime,
    to: LocalDateTime
  ): List<ReminderV2> = emptyList()
  override suspend fun getByGroupId(groupId: String): List<ReminderV2> = byGroupId[groupId].orEmpty()
  override suspend fun countActiveByGroupId(groupId: String): Int = 0
  override suspend fun getByNoteId(noteId: String): List<ReminderV2> = emptyList()
  override suspend fun search(query: String): List<ReminderV2> = emptyList()
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll(ids: List<String>) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun clearGroupId(groupId: String) = Unit
}
