package com.github.naz013.repository.impl

import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.repository.dao.HabitCheckInDao
import com.github.naz013.repository.dao.HabitDao
import com.github.naz013.repository.entity.HabitCheckInEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.every
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitRepositoryImplTest {
  private val habitDao = mockk<HabitDao>(relaxed = true)
  private val checkInDao = mockk<HabitCheckInDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: HabitRepositoryImpl

  @Before
  fun setUp() {
    repository = HabitRepositoryImpl(habitDao, checkInDao, notifier)
  }

  @Test
  fun `delete removes the habit together with its check-ins in one DAO transaction`() = runTest {
    repository.delete("h-1")

    verify(exactly = 1) { habitDao.deleteWithCheckIns("h-1") }
    verify { notifier.notify(Table.Habit) }
    verify { notifier.notify(Table.HabitCheckIn) }
  }

  @Test
  fun `deleteAll clears all habits and check-ins in one DAO transaction`() = runTest {
    repository.deleteAll()

    verify(exactly = 1) { habitDao.deleteAllWithCheckIns() }
    verify { notifier.notify(Table.Habit) }
    verify { notifier.notify(Table.HabitCheckIn) }
  }

  @Test
  fun `saveCheckIn upserts the entity keyed by habit and ISO date and notifies`() = runTest {
    val entity = slot<HabitCheckInEntity>()
    every { checkInDao.insert(capture(entity)) } returns Unit

    repository.saveCheckIn(
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
  fun `getCheckIn returns null when no row exists`() = runTest {
    every { checkInDao.get("h-1", "2026-10-18") } returns null

    assertEquals(null, repository.getCheckIn("h-1", LocalDate.of(2026, 10, 18)))
  }
}
