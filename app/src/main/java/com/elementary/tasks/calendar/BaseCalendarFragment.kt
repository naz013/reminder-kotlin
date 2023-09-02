package com.elementary.tasks.calendar

import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.calendar.monthview.DayBottomSheetDialog
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.navigation.fragments.BaseAnimatedFragment
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate

abstract class BaseCalendarFragment<B : ViewBinding> : BaseAnimatedFragment<B>() {

  protected val dateTimeManager by inject<DateTimeManager>()

  protected var date: LocalDate = LocalDate.now()
  private var mDialog: AlertDialog? = null
  private var job: Job? = null
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { }
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
      withActivity {
        PinLoginActivity.openLogged(it, CreateReminderActivity::class.java) {
          putExtra(Constants.INTENT_DATE, date)
        }
      }
    }
  }

  protected fun addBirthday() {
    if (isAdded) {
      withActivity {
        PinLoginActivity.openLogged(it, AddBirthdayActivity::class.java) {
          putExtra(Constants.INTENT_DATE, date)
        }
      }
    }
  }
}
