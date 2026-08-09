package com.github.naz013.repository.impl

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.repository.dao.HolidayDao
import com.github.naz013.repository.entity.HolidayEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class HolidayRepositoryImplTest {
  private val dao = mockk<HolidayDao>(relaxed = true)
  private val notifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: HolidayRepositoryImpl

  @Before
  fun setUp() {
    repository = HolidayRepositoryImpl(dao, notifier)
  }

  private fun holiday(
    name: String = "New Year's Day",
    date: LocalDate = LocalDate.of(2026, 1, 1),
    countryCode: String = "US",
  ) = PublicHoliday(
    id = PublicHoliday.id(countryCode, date, name),
    countryCode = countryCode,
    date = date,
    name = name,
    nameLocal = name,
    type = "Public",
    location = null,
  )

  @Test
  fun `getByDateRange maps every entity row to a domain PublicHoliday`() = runTest {
    val start = LocalDate.of(2026, 1, 1)
    val end = LocalDate.of(2026, 1, 31)
    coEvery { dao.getByDateRange("US", start.toEpochDay(), end.toEpochDay()) } returns
      listOf(HolidayEntity(holiday()))

    val result = repository.getByDateRange("US", start, end)

    assertEquals(listOf(holiday()), result)
  }

  @Test
  fun `getByDate returns null when nothing is cached for that day`() = runTest {
    val date = LocalDate.of(2026, 1, 1)
    coEvery { dao.getByDate("US", date.toEpochDay()) } returns null

    assertEquals(null, repository.getByDate("US", date))
  }

  @Test
  fun `replaceForYear deletes the country's year before inserting, then notifies`() = runTest {
    val holidays = listOf(holiday())

    repository.replaceForYear("US", 2026, holidays)

    coVerifyOrder {
      dao.deleteForCountryAndYear("US", 2026)
      dao.insertAll(holidays.map { HolidayEntity(it) })
    }
    coVerify { notifier.notify(Table.Holiday) }
  }

  @Test
  fun `deleteForCountry clears the dao and notifies`() = runTest {
    repository.deleteForCountry("US")

    coVerify { dao.deleteForCountry("US") }
    coVerify { notifier.notify(Table.Holiday) }
  }

  @Test
  fun `deleteAll clears the dao and notifies`() = runTest {
    repository.deleteAll()

    coVerify { dao.deleteAll() }
    coVerify { notifier.notify(Table.Holiday) }
  }
}
