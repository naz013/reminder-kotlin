package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class CountActiveRemindersV2ByGroupIdUseCaseTest {

  @Test
  fun `returns the count reported by the repository for the group`() = runTest {
    val repository = FakeCountReminderV2Repository(mapOf("group-1" to 3))
    val useCase = CountActiveRemindersV2ByGroupIdUseCase(repository)

    val result = useCase("group-1")

    assertEquals(3, result)
  }

  @Test
  fun `returns zero when the group has no active reminders`() = runTest {
    val repository = FakeCountReminderV2Repository(emptyMap())
    val useCase = CountActiveRemindersV2ByGroupIdUseCase(repository)

    val result = useCase("group-1")

    assertEquals(0, result)
  }
}

private class FakeCountReminderV2Repository(
  private val counts: Map<String, Int>
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
  override suspend fun getByGroupId(groupId: String): List<ReminderV2> = emptyList()
  override suspend fun countActiveByGroupId(groupId: String): Int = counts[groupId] ?: 0
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
