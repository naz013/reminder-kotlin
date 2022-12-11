package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateSingleBackupWorker

class SmsTemplateViewModel(
  key: String,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  smsTemplatesDao: SmsTemplatesDao
) : BaseSmsTemplatesViewModel(prefs, dispatcherProvider, workManagerProvider, smsTemplatesDao) {

  val smsTemplate = smsTemplatesDao.loadByKey(key)
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false

  fun findSame(id: String) {
    launchDefault {
      val template = smsTemplatesDao.getByKey(id)
      hasSameInDb = template != null
    }
  }

  fun saveTemplate(smsTemplate: SmsTemplate) {
    postInProgress(true)
    launchDefault {
      smsTemplatesDao.insert(smsTemplate)
      startWork(TemplateSingleBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
