package com.github.naz013.feature.settings.proversion

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProBuyClickedEvent
import com.github.naz013.analytics.ProScreenViewedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class ProVersionViewModelTest : BaseTest() {
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  @Test
  fun `builds the advantages list from text provider in declared order`() {
    every { textProvider.getString(R.string.pro_streaks_and_insights) } returns "Streaks & Insights"
    every { textProvider.getString(R.string.pro_local_encrypted_backup) } returns "Local encrypted backup"
    every { textProvider.getString(R.string.pro_gemini_app_functions) } returns "Gemini AppFunctions"
    every { textProvider.getString(R.string.pro_public_holidays) } returns "Public holidays"
    every { textProvider.getString(R.string.pro_no_ads) } returns "No ads"
    every { textProvider.getString(R.string.pro_icalendar_custom_reminder) } returns "iCalendar custom reminder"
    every { textProvider.getString(R.string.additional_font_styles_for_notes) } returns "More note fonts"
    every { textProvider.getString(R.string.pro_different_settings_for_birthdays) } returns "Birthday settings"
    every { textProvider.getString(R.string.pro_led_notification) } returns "LED notification"
    every { textProvider.getString(R.string.pro_led_color_for_each_reminder) } returns "LED color per reminder"
    every { textProvider.getString(R.string.pro_styles_for_marker) } returns "Marker styles"

    val viewModel = ProVersionViewModel(textProvider, analyticsEventSender)

    assertEquals(
      listOf(
        "Streaks & Insights",
        "Local encrypted backup",
        "Gemini AppFunctions",
        "Public holidays",
        "No ads",
        "iCalendar custom reminder",
        "More note fonts",
        "Birthday settings",
        "LED notification",
        "LED color per reminder",
        "Marker styles",
      ),
      viewModel.state.advantages,
    )
  }

  @Test
  fun `sends ProScreenViewedEvent on init`() {
    ProVersionViewModel(textProvider, analyticsEventSender)

    verify { analyticsEventSender.send(ProScreenViewedEvent) }
  }

  @Test
  fun `sends ProBuyClickedEvent when buy is clicked`() {
    val viewModel = ProVersionViewModel(textProvider, analyticsEventSender)

    viewModel.onBuyClicked()

    verify { analyticsEventSender.send(ProBuyClickedEvent) }
  }
}
