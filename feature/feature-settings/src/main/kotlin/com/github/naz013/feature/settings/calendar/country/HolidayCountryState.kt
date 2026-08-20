package com.github.naz013.feature.settings.calendar.country

internal data class HolidayCountryState(
  val listState: CountryListState = CountryListState.Loading,
  val searchQuery: String = "",
  val selectedCode: String = "",
)

internal sealed interface CountryListState {
  data object Loading : CountryListState

  data class Ready(
    val countries: List<UiCountry>,
  ) : CountryListState

  data object Empty : CountryListState
}

internal data class UiCountry(
  val code: String,
  val name: String,
  val flagEmoji: String,
)
