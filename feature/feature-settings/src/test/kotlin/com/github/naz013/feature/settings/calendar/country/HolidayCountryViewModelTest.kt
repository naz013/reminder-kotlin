package com.github.naz013.feature.settings.calendar.country

import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HolidayCountryViewModelTest : BaseTest() {
  private val prefs = mockk<CalendarSettingsPreferences>()
  private lateinit var viewModel: HolidayCountryViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.holidayCountryCode } returns "US"

    viewModel = HolidayCountryViewModel(mockDispatcherProvider(), prefs)
  }

  @Test
  fun `seeds selectedCode from prefs on creation`() {
    assertEquals("US", viewModel.state.value.selectedCode)
  }

  @Test
  fun `loads every ISO country, flags included, sorted by name`() {
    val listState = viewModel.state.value.listState as? CountryListState.Ready

    assertTrue((listState?.countries?.size ?: 0) > 100)
    val unitedStates = listState?.countries?.first { it.code == "US" }
    assertEquals("🇺🇸", unitedStates?.flagEmoji)
    assertEquals(
      listState?.countries?.map { it.name },
      listState?.countries?.map { it.name }?.sorted(),
    )
  }

  @Test
  fun `onSearchQueryChange filters the list by name, case-insensitively`() {
    // Country display names are resolved via java.util.Locale against the JVM's default locale,
    // which varies by machine - derive the query from the same source rather than hardcoding an
    // English name like "United States", so this test doesn't depend on the CI/dev machine's locale.
    val usName = java.util.Locale("", "US").displayCountry
    val partialQuery = usName.take((usName.length / 2).coerceAtLeast(1)).uppercase()

    viewModel.onSearchQueryChange(partialQuery)

    val listState = viewModel.state.value.listState as? CountryListState.Ready
    assertTrue(listState?.countries?.any { it.code == "US" } == true)
    assertTrue(listState?.countries?.all { it.name.contains(partialQuery, ignoreCase = true) } == true)
  }

  @Test
  fun `onSearchQueryChange with no matches yields Empty`() {
    viewModel.onSearchQueryChange("zzzzzzzzzz")

    assertEquals(CountryListState.Empty, viewModel.state.value.listState)
  }

  @Test
  fun `onSearchQueryChange with a blank query restores the full list`() {
    viewModel.onSearchQueryChange("united states")
    viewModel.onSearchQueryChange("")

    val listState = viewModel.state.value.listState as? CountryListState.Ready
    assertTrue((listState?.countries?.size ?: 0) > 100)
  }

  @Test
  fun `onCountryClick emits CountrySelected with the clicked country's code`() {
    viewModel.onCountryClick(UiCountry(code = "FR", name = "France", flagEmoji = "🇫🇷"))

    val event = viewModel.event.value?.peekContent()
    assertEquals(HolidayCountryViewModel.ViewModelEvent.CountrySelected("FR"), event)
  }
}
