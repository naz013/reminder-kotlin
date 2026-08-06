package com.elementary.tasks.settings.location

import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MapStyleViewModel(
  private val prefs: Prefs,
) : ViewModel() {
  val state: StateFlow<MapStyleState> field = MutableStateFlow(buildState())

  fun onOptionSelected(index: Int) {
    prefs.mapStyle = index
    state.update { it.copy(selectedIndex = index) }
  }

  private fun buildState() =
    MapStyleState(
      options = OPTIONS,
      selectedIndex = prefs.mapStyle,
    )

  companion object {
    private const val MAP_STYLE_AUTO = 6

    private val OPTIONS =
      listOf(
        MapStyleOption(
          index = MAP_STYLE_AUTO,
          titleRes = R.string.auto,
          previews = listOf(R.drawable.preview_map_day, R.drawable.preview_map_night),
        ),
        MapStyleOption(index = 0, titleRes = R.string.day, previews = listOf(R.drawable.preview_map_day)),
        MapStyleOption(index = 1, titleRes = R.string.retro, previews = listOf(R.drawable.preview_map_retro)),
        MapStyleOption(index = 2, titleRes = R.string.silver, previews = listOf(R.drawable.preview_map_silver)),
        MapStyleOption(index = 3, titleRes = R.string.night, previews = listOf(R.drawable.preview_map_night)),
        MapStyleOption(index = 4, titleRes = R.string.dark, previews = listOf(R.drawable.preview_map_dark)),
        MapStyleOption(
          index = 5,
          titleRes = R.string.aubergine,
          previews = listOf(R.drawable.preview_map_aubergine),
        ),
      )
  }
}
