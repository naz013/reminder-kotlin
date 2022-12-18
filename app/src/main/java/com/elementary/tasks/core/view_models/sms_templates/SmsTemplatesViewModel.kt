package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.DispatcherProvider

class SmsTemplatesViewModel(
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  smsTemplatesDao: SmsTemplatesDao
) : BaseSmsTemplatesViewModel(dispatcherProvider, workerLauncher, smsTemplatesDao) {
  val smsTemplates = smsTemplatesDao.loadAll()
}
