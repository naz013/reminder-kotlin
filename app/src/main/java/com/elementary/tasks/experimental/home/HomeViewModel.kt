package com.elementary.tasks.experimental.home

import android.content.Context
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel
import org.koin.core.inject

class HomeViewModel : BaseRemindersViewModel() {

    private val context: Context by inject()

    val reminders = appDb.reminderDao().loadAllTypesInRange(limit = 5,
            fromTime = TimeUtil.getDayStart(), toTime = TimeUtil.getDayEnd())

    val birthdays = appDb.birthdaysDao().loadAll(TimeUtil.getBirthdayDayMonth())

    fun deleteBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            appDb.birthdaysDao().delete(birthday)
            updateBirthdayPermanent()
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
            postInProgress(false)
            postCommand(Commands.DELETED)
        }
    }

    private fun updateBirthdayPermanent() {
        if (prefs.isBirthdayPermanentEnabled) {
            Notifier.showBirthdayPermanent(context, prefs)
        }
    }
}
