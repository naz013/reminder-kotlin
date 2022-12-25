package com.elementary.tasks.core.view_models.sms_templates

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateSingleBackupWorker
import kotlinx.coroutines.launch

class SmsTemplateViewModel(
  key: String,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  smsTemplatesDao: SmsTemplatesDao
) : BaseSmsTemplatesViewModel(dispatcherProvider, workerLauncher, smsTemplatesDao) {

  val smsTemplate = smsTemplatesDao.loadByKey(key)
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val template = smsTemplatesDao.getByKey(id)
      hasSameInDb = template != null
    }
  }

  fun saveTemplate(smsTemplate: SmsTemplate) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      smsTemplatesDao.insert(smsTemplate)
      workerLauncher.startWork(TemplateSingleBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
