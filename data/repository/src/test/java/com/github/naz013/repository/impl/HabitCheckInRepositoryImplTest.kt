package com.github.naz013.repository.impl

import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.repository.dao.HabitCheckInDao
import com.github.naz013.repository.entity.HabitCheckInEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitCheckInRepositoryImplTest {
  private val dao = mockk<HabitCheckInDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: HabitCheckInRepositoryImpl

  @Before
  fun setUp() {
    repository = HabitCheckInRepositoryImpl(dao, notifier)
  }

  @Test
  fun `save upserts the entity keyed by habit and ISO date and notifies`() = runTest {
    val entity = slot<HabitCheckInEntity>()
    every { dao.insert(capture(entity)) } returns Unit

    repository.save(
      HabitCheckIn(
        habitId = "h-1",
        date = LocalDate.of(2026, 10, 18),
        count = 1,
        createdAt = LocalDateTime.of(2026, 10, 18, 7, 0),
        updatedAt = LocalDateTime.of(2026, 10, 18, 7, 0)
      )
    )

    assertEquals("h-1", entity.captured.habitId)
    assertEquals("2026-10-18", entity.captured.date)
    verify { notifier.notify(Table.HabitCheckIn) }
  }

  @Test
  fun `get returns null when no row exists`() = runTest {
    every { dao.get("h-1", "2026-10-18") } returns null

    assertEquals(null, repository.get("h-1", LocalDate.of(2026, 10, 18)))
  }

  @Test
  fun `getRange passes ISO bounds to the DAO`() = runTest {
    every { dao.getRange("h-1", "2026-10-12", "2026-10-18") } returns emptyList()

    val result = repository.getRange("h-1", LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 18))

    assertEquals(emptyList<HabitCheckIn>(), result)
  }
}
