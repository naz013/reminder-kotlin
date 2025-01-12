package com.elementary.tasks.calendar

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.calendar.monthview.DayBottomSheetDialog
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.navigation.topfragment.BaseTopToolbarFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.build.BuildReminderActivity
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.login.LoginApi
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

abstract class BaseCalendarFragment<B : ViewBinding> : BaseTopToolbarFragment<B>() {

  protected val dateTimeManager by inject<DateTimeManager>()

  protected var date: LocalDate = LocalDate.now()
  private var mDialog: AlertDialog? = null
  private var job: Job? = null
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { },
    birthdayEditAction = {
      navigate {
        navigate(
          R.id.editBirthdayFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_ID, it.uuId)
          }
        )
      }
    },
    birthdayOpenAction = {
      navigate {
        navigate(
          R.id.previewBirthdayFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_ID, it.uuId)
          }
        )
      }
    }
  )
  private val reminderResolver = ReminderResolver(
    dialogAction = { dialogues },
    toggleAction = { },
    deleteAction = { },
    skipAction = { }
  )

  protected fun showActionDialog() {
    safeContext {
      DayBottomSheetDialog(
        context = this,
        label = dateTimeManager.formatCalendarDate(date),
        addReminderCallback = { addReminder() },
        addBirthdayCallback = { addBirthday() }
      ).show()
    }
  }

  protected fun addReminder() {
    if (isAdded) {
      val deepLinkData = ReminderDatetimeTypeDeepLinkData(
        type = Reminder.BY_DATE,
        dateTime = LocalDateTime.of(date, LocalTime.now())
      )
      withActivity {
        LoginApi.openLogged(it, BuildReminderActivity::class.java, deepLinkData)
      }
    }
  }

  protected fun addBirthday() {
    if (isAdded) {
      val deepLinkData = BirthdayDateDeepLinkData(date)
      navigate {
        navigate(
          R.id.editBirthdayFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          }
        )
      }
    }
  }
}
