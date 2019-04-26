package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.navigation.settings.additional.work.DeleteBackupWorker
import kotlinx.coroutines.runBlocking

abstract class BaseSmsTemplatesViewModel : BaseDbViewModel() {

    fun deleteSmsTemplate(smsTemplate: SmsTemplate) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                appDb.smsTemplatesDao().delete(smsTemplate)
            }
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
            postInProgress(false)
            postCommand(Commands.DELETED)
        }
    }
}
