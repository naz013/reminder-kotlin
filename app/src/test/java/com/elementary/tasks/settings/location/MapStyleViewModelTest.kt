package com.elementary.tasks.settings.location

import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapStyleViewModelTest : BaseTest() {
  private val prefs = mockk<Prefs>()

  private var mapStyle = 6

  private lateinit var viewModel: MapStyleViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { prefs.mapStyle } answers { mapStyle }
    every { prefs.mapStyle = any() } answers { mapStyle = firstArg() }

    viewModel = MapStyleViewModel(prefs)
  }

  @Test
  fun `builds initial state with all map style options and current selection`() {
    mapStyle = 3
    viewModel = MapStyleViewModel(prefs)

    val state = viewModel.state.value

    assertEquals(7, state.options.size)
    assertEquals(3, state.selectedIndex)
    val autoOption = state.options.first()
    assertEquals(6, autoOption.index)
    assertEquals(R.string.auto, autoOption.titleRes)
    assertEquals(listOf(R.drawable.preview_map_day, R.drawable.preview_map_night), autoOption.previews)
  }

  @Test
  fun `onOptionSelected persists the chosen style and updates selected index`() {
    mapStyle = 6

    viewModel.onOptionSelected(2)

    assertEquals(2, mapStyle)
    assertEquals(2, viewModel.state.value.selectedIndex)
  }
}
