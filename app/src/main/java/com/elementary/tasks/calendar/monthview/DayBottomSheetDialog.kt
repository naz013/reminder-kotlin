package com.elementary.tasks.calendar.monthview

import android.content.Context
import com.elementary.tasks.R
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.lottie.LottieAnimation
import com.maxkeppeler.sheets.lottie.withCoverLottieAnimation

class DayBottomSheetDialog(
  private val context: Context,
  private val label: String,
  private val addReminderCallback: () -> Unit,
  private val addBirthdayCallback: () -> Unit
) {

  fun show() {
    InfoSheet().show(context) {
      title(label)
      withCoverLottieAnimation(
        LottieAnimation {
          setupAnimation {
            setAnimation(R.raw.calendar_dialog_animation)
          }
        }
      )
      displayCloseButton(true)
      displayPositiveButton(true)
      displayNegativeButton(true)
      onPositive(R.string.add_reminder_menu) {
        addReminderCallback()
      }
      onNegative(R.string.add_birthday) {
        addBirthdayCallback()
      }
    }
  }
}
