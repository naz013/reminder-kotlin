package com.elementary.tasks.calendar

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.calendar.monthview.DayBottomSheetDialog
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.topfragment.BaseTopToolbarFragment
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

abstract class BaseCalendarFragment<B : ViewBinding> : BaseTopToolbarFragment<B>() {

  protected val dateTimeManager by inject<DateTimeManager>()

  protected fun showActionDialog(date: LocalDate) {
    safeContext {
      DayBottomSheetDialog(
        context = this,
        label = dateTimeManager.formatCalendarDate(date),
        addReminderCallback = { addReminder(date) },
        addBirthdayCallback = { addBirthday(date) }
      ).show()
    }
  }

  protected fun addReminder(date: LocalDate) {
    if (isAdded) {
      val deepLinkData = ReminderDatetimeTypeDeepLinkData(
        type = Reminder.BY_DATE,
        dateTime = LocalDateTime.of(date, LocalTime.now())
      )
      navigate {
        navigate(
          R.id.buildReminderFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          },
          NavigationAnimations.inDepthNavOptions()
        )
      }
    }
  }

  protected fun addBirthday(date: LocalDate) {
    if (isAdded) {
      val deepLinkData = BirthdayDateDeepLinkData(date)
      navigate {
        navigate(
          R.id.editBirthdayFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          },
          NavigationAnimations.inDepthNavOptions()
        )
      }
    }
  }
}
