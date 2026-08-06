package com.elementary.tasks.settings.proversion

import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.github.naz013.common.TextProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ProVersionViewModelTest : BaseTest() {
  private val textProvider = mockk<TextProvider>()

  @Test
  fun `builds the advantages list from text provider in declared order`() {
    every { textProvider.getString(R.string.pro_no_ads) } returns "No ads"
    every { textProvider.getString(R.string.additional_recur_type) } returns "More recurrence types"
    every { textProvider.getString(R.string.additional_font_styles_for_notes) } returns "More note fonts"
    every { textProvider.getString(R.string.pro_different_settings_for_birthdays) } returns "Birthday settings"
    every { textProvider.getString(R.string.pro_led_notification) } returns "LED notification"
    every { textProvider.getString(R.string.pro_led_color_for_each_reminder) } returns "LED color per reminder"
    every { textProvider.getString(R.string.pro_styles_for_marker) } returns "Marker styles"

    val viewModel = ProVersionViewModel(textProvider)

    assertEquals(
      listOf(
        "No ads",
        "More recurrence types",
        "More note fonts",
        "Birthday settings",
        "LED notification",
        "LED color per reminder",
        "Marker styles",
      ),
      viewModel.state.advantages,
    )
  }
}
