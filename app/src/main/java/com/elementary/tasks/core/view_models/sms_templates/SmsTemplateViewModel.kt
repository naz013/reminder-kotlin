package com.elementary.tasks.core.view_models.sms_templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.navigation.settings.additional.work.SingleBackupWorker
import kotlinx.coroutines.runBlocking

class SmsTemplateViewModel private constructor(key: String) : BaseSmsTemplatesViewModel() {

  val smsTemplate = appDb.smsTemplatesDao().loadByKey(key)
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false

  fun findSame(id: String) {
    launchDefault {
      val template = appDb.smsTemplatesDao().getByKey(id)
      hasSameInDb = template != null
    }
  }

  fun saveTemplate(smsTemplate: SmsTemplate) {
    postInProgress(true)
    launchDefault {
      runBlocking {
        appDb.smsTemplatesDao().insert(smsTemplate)
      }
      startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, smsTemplate.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  class Factory(private val key: String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return SmsTemplateViewModel(key) as T
    }
  }
}
