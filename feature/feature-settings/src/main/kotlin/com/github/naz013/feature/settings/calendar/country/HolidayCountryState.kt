package com.github.naz013.feature.settings.calendar.country

data class HolidayCountryState(
  val listState: CountryListState = CountryListState.Loading,
  val searchQuery: String = "",
  val selectedCode: String = "",
)

sealed interface CountryListState {
  data object Loading : CountryListState

  data class Ready(
    val countries: List<UiCountry>,
  ) : CountryListState

  data object Empty : CountryListState
}

data class UiCountry(
  val code: String,
  val name: String,
  val flagEmoji: String,
)
