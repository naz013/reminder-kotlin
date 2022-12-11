package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker

abstract class BaseSmsTemplatesViewModel(
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  protected val smsTemplatesDao: SmsTemplatesDao
) : BaseDbViewModel(prefs, dispatcherProvider, workManagerProvider) {

  fun deleteSmsTemplate(smsTemplate: SmsTemplate) {
    postInProgress(true)
    launchDefault {
      smsTemplatesDao.delete(smsTemplate)
      startWork(TemplateDeleteBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
