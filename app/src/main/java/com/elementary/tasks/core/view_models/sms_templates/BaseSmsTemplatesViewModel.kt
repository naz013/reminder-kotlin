package com.elementary.tasks.core.view_models.sms_templates

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker
import kotlinx.coroutines.launch

abstract class BaseSmsTemplatesViewModel(
  dispatcherProvider: DispatcherProvider,
  protected val workerLauncher: WorkerLauncher,
  protected val smsTemplatesDao: SmsTemplatesDao
) : BaseProgressViewModel(dispatcherProvider) {

  fun deleteSmsTemplate(smsTemplate: SmsTemplate) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      smsTemplatesDao.delete(smsTemplate)
      workerLauncher.startWork(TemplateDeleteBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
