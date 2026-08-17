package com.github.naz013.feature.settings.proversion

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProBuyClickedEvent
import com.github.naz013.analytics.ProScreenViewedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.ui.common.R

class ProVersionViewModel(
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {
  val state: ProVersionState =
    ProVersionState(
      advantages =
        listOf(
          R.string.pro_streaks_and_insights,
          R.string.pro_local_encrypted_backup,
          R.string.pro_gemini_app_functions,
          R.string.pro_public_holidays,
          R.string.pro_no_ads,
          R.string.pro_icalendar_custom_reminder,
          R.string.additional_font_styles_for_notes,
          R.string.pro_different_settings_for_birthdays,
          R.string.pro_led_notification,
          R.string.pro_led_color_for_each_reminder,
          R.string.pro_styles_for_marker,
        ).map { textProvider.getString(it) },
    )

  init {
    analyticsEventSender.send(ProScreenViewedEvent)
  }

  fun onBuyClicked() {
    analyticsEventSender.send(ProBuyClickedEvent)
  }
}
