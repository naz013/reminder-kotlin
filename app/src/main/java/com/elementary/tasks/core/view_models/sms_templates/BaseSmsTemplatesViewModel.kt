package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker

abstract class BaseSmsTemplatesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider
) : BaseDbViewModel(appDb, prefs, dispatcherProvider) {

  fun deleteSmsTemplate(smsTemplate: SmsTemplate) {
    postInProgress(true)
    launchDefault {
      appDb.smsTemplatesDao().delete(smsTemplate)
      startWork(TemplateDeleteBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
