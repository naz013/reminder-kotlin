package com.elementary.tasks.sms.list

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.sms.UiSmsListAdapter
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker
import kotlinx.coroutines.launch

class SmsTemplatesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val smsTemplatesDao: SmsTemplatesDao,
  private val uiSmsListAdapter: UiSmsListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  val smsTemplates = Transformations.map(smsTemplatesDao.loadAll()) { list ->
    list.map { uiSmsListAdapter.convert(it) }
  }

  fun deleteSmsTemplate(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val smsTemplate = smsTemplatesDao.getByKey(id) ?: return@launch
      smsTemplatesDao.delete(smsTemplate)
      workerLauncher.startWork(
        TemplateDeleteBackupWorker::class.java,
        Constants.INTENT_ID,
        smsTemplate.key
      )
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
