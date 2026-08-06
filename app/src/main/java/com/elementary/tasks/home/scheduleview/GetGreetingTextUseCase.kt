package com.elementary.tasks.home.scheduleview

import com.elementary.tasks.R
import com.github.naz013.common.TextProvider
import org.threeten.bp.LocalTime

class GetGreetingTextUseCase(
  private val textProvider: TextProvider,
) {
  /**
   * Returns greeting text based on current time.
   * Morning: 00:00 - 11:59
   * Afternoon: 12:00 - 17:59
   * Evening: 18:00 - 23:59
   */
  operator fun invoke(time: LocalTime = LocalTime.now()): String =
    when (time) {
      in Morning..<Noon -> {
        textProvider.getString(R.string.schedule_good_morning)
      }
      in Noon..<Evening -> {
        textProvider.getString(R.string.schedule_good_afternoon)
      }
      else -> {
        textProvider.getString(R.string.schedule_good_evening)
      }
    }

  companion object {
    private val Morning = LocalTime.of(0, 0)
    private val Noon = LocalTime.of(12, 0)
    private val Evening = LocalTime.of(18, 0)
  }
}
