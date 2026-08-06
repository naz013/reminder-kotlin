package com.elementary.tasks.settings.proversion

import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.github.naz013.common.TextProvider

class ProVersionViewModel(
  private val textProvider: TextProvider,
) : ViewModel() {
  val state: ProVersionState =
    ProVersionState(
      advantages =
        listOf(
          R.string.pro_no_ads,
          R.string.additional_recur_type,
          R.string.additional_font_styles_for_notes,
          R.string.pro_different_settings_for_birthdays,
          R.string.pro_led_notification,
          R.string.pro_led_color_for_each_reminder,
          R.string.pro_styles_for_marker,
        ).map { textProvider.getString(it) },
    )
}
