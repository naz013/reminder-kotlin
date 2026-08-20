package com.github.naz013.feature.settings.calendar.country

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

internal class HolidayCountryViewModel(
  private val dispatcherProvider: DispatcherProvider,
  prefs: CalendarSettingsPreferences,
) : ViewModel() {

  val state: StateFlow<HolidayCountryState> field =
    MutableStateFlow(HolidayCountryState(selectedCode = prefs.holidayCountryCode))
  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var allCountries: List<UiCountry> = emptyList()

  init {
    loadCountries()
  }

  fun onSearchQueryChange(query: String) {
    state.update { it.copy(searchQuery = query) }
    applyFilter(query)
  }

  fun onCountryClick(country: UiCountry) {
    event.emit(ViewModelEvent.CountrySelected(country.code))
  }

  private fun loadCountries() {
    viewModelScope.launch(dispatcherProvider.default()) {
      allCountries = Locale.getISOCountries()
        .map { code ->
          UiCountry(
            code = code,
            name = Locale("", code).displayCountry.ifBlank { code },
            flagEmoji = countryCodeToFlagEmoji(code),
          )
        }
        .sortedBy { it.name }

      applyFilter(state.value.searchQuery)
    }
  }

  private fun applyFilter(query: String) {
    val filtered = if (query.isBlank()) {
      allCountries
    } else {
      allCountries.filter { it.name.contains(query, ignoreCase = true) }
    }
    state.update {
      it.copy(listState = if (filtered.isEmpty()) CountryListState.Empty else CountryListState.Ready(filtered))
    }
  }

  sealed interface ViewModelEvent {
    data class CountrySelected(
      val code: String,
    ) : ViewModelEvent
  }
}
