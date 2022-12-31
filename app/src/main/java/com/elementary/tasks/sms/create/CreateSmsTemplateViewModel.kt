package com.elementary.tasks.sms.create

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.settings.additional.work.TemplateDeleteBackupWorker
import com.elementary.tasks.settings.additional.work.TemplateSingleBackupWorker
import kotlinx.coroutines.launch
import java.util.UUID

class CreateSmsTemplateViewModel(
  private val id: String,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val smsTemplatesDao: SmsTemplatesDao,
  private val analyticsEventSender: AnalyticsEventSender,
  private val contextProvider: ContextProvider
) : BaseProgressViewModel(dispatcherProvider) {

  private val _smsTemplate = mutableLiveDataOf<SmsTemplate>()
  val smsTemplate = _smsTemplate.toLiveData()

  private var isEdited = false
  private var editedTemplate: SmsTemplate? = null
  var hasSameInDb: Boolean = false
    private set
  var isFromFile: Boolean = false
    private set

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    viewModelScope.launch(dispatcherProvider.default()) {
      val smsTemplate = smsTemplatesDao.getByKey(id) ?: return@launch
      onLoaded(smsTemplate)
    }
  }

  fun canDelete(): Boolean {
    return editedTemplate != null && !isFromFile
  }

  fun loadFromFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
        runCatching {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri)
          if (any != null && any is SmsTemplate) {
            onLoaded(any)
            findSame(any.key)
          }
        }
      }
    }
  }

  fun loadFromIntent(smsTemplate: SmsTemplate?) {
    if (smsTemplate != null) {
      onLoaded(smsTemplate)
      findSame(smsTemplate.key)
    }
  }

  fun saveTemplate(text: String, newId: Boolean = false) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val date = DateTimeManager.gmtDateTime
      val smsTemplate = (editedTemplate ?: SmsTemplate()).apply {
        this.date = date
        this.title = text
      }
      if (newId) {
        smsTemplate.key = UUID.randomUUID().toString()
      }
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_SMS_TEMPLATE))
      smsTemplatesDao.insert(smsTemplate)
      workerLauncher.startWork(
        TemplateSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        smsTemplate.key
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun deleteSmsTemplate() {
    val smsTemplate = editedTemplate ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
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

  private fun onLoaded(smsTemplate: SmsTemplate) {
    if (!isEdited) {
      editedTemplate = smsTemplate
      _smsTemplate.postValue(smsTemplate)
      isEdited = true
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val template = smsTemplatesDao.getByKey(id)
      hasSameInDb = template != null
    }
  }
}
