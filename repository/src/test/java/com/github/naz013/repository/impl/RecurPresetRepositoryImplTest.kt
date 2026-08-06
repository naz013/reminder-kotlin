package com.github.naz013.repository.impl

import com.github.naz013.domain.PresetType
import com.github.naz013.repository.dao.RecurPresetDao
import com.github.naz013.repository.entity.RecurPresetEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RecurPresetRepositoryImplTest {
  private val dao = mockk<RecurPresetDao>()
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: RecurPresetRepositoryImpl

  @Before
  fun setUp() {
    repository = RecurPresetRepositoryImpl(dao, notifier)
  }

  private fun presetEntity(id: String, type: PresetType) = RecurPresetEntity(
    id = id,
    recurObject = "",
    name = "name",
    type = type,
    createdAt = LocalDateTime.now(),
    useCount = 0,
    description = null,
    isDefault = false,
    recurItemsToAdd = null
  )

  @Test
  fun `getAllByType falls back to getAll when the type is null`() = runTest {
    every { dao.getAll() } returns listOf(presetEntity("1", PresetType.RECUR))

    val result = repository.getAllByType(null)

    assertEquals(listOf("1"), result.map { it.id })
  }

  @Test
  fun `getAllByType queries by ordinal when a type is given`() = runTest {
    every { dao.getAllByType(PresetType.BUILDER.ordinal) } returns listOf(presetEntity("2", PresetType.BUILDER))

    val result = repository.getAllByType(PresetType.BUILDER)

    assertEquals(listOf("2"), result.map { it.id })
  }
}
