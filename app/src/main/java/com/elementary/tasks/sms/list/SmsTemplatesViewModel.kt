package com.elementary.tasks.sms.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.sms.UiSmsListAdapter
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

@Deprecated("After S")
class SmsTemplatesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val smsTemplatesDao: SmsTemplatesDao,
  private val uiSmsListAdapter: UiSmsListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val templatesData = SearchableData(dispatcherProvider, viewModelScope, smsTemplatesDao)
  val smsTemplates = templatesData.map { list ->
    list.map { uiSmsListAdapter.convert(it) }
  }

  fun onSearchUpdate(query: String) {
    templatesData.onNewQuery(query)
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
      templatesData.refresh()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  internal class SearchableData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val smsTemplatesDao: SmsTemplatesDao
  ) : SearchableLiveData<List<SmsTemplate>>(parentScope + dispatcherProvider.default()) {

    override fun runQuery(query: String): List<SmsTemplate> {
      return if (query.isEmpty()) {
        smsTemplatesDao.getAll()
      } else {
        smsTemplatesDao.searchByTitle(query.lowercase())
      }
    }
  }
}
