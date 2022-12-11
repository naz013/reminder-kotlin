package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.view_models.DispatcherProvider

class SmsTemplatesViewModel(
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  smsTemplatesDao: SmsTemplatesDao
) : BaseSmsTemplatesViewModel(prefs, dispatcherProvider, workManagerProvider, smsTemplatesDao) {
  val smsTemplates = smsTemplatesDao.loadAll()
}
