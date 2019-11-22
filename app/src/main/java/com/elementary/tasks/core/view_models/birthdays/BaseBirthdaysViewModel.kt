package com.elementary.tasks.core.view_models.birthdays

import android.content.Context
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import org.koin.core.inject

abstract class BaseBirthdaysViewModel : BaseDbViewModel() {

    private val context: Context by inject()

    fun deleteBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            appDb.birthdaysDao().delete(birthday)
            updateBirthdayPermanent()
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId, context)
            postInProgress(false)
            postCommand(Commands.DELETED)
        }
    }

    protected fun updateBirthdayPermanent() {
        if (prefs.isBirthdayPermanentEnabled) {
            Notifier.showBirthdayPermanent(context, prefs)
        }
    }

    fun saveBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            birthday.updatedAt = TimeUtil.gmtDateTime
            appDb.birthdaysDao().insert(birthday)
            updateBirthdayPermanent()
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId, context)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }
}
