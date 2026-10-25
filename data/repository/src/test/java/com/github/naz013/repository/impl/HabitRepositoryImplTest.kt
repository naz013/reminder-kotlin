package com.github.naz013.repository.impl

import com.github.naz013.repository.dao.HabitDao
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import com.github.naz013.repository.entity.HabitEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class HabitRepositoryImplTest {
  private val habitDao = mockk<HabitDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: HabitRepositoryImpl

  @Before
  fun setUp() {
    repository = HabitRepositoryImpl(habitDao, notifier)
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
  fun `observeById maps the stored habit to the domain model`() = runTest {
    val entity = HabitEntity(id = "h-1", name = "Run", frequency = "DAILY", createdAt = 0L, updatedAt = 0L, syncState = "Synced")
    every { habitDao.observeById("h-1") } returns flowOf(entity)

    assertEquals("Run", repository.observeById("h-1").first()?.name)
  }

  @Test
  fun `observeById emits null when the habit is missing`() = runTest {
    every { habitDao.observeById("missing") } returns flowOf(null)

    assertNull(repository.observeById("missing").first())
  }
}
