package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.view_models.DispatcherProvider

class SmsTemplatesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider
) : BaseSmsTemplatesViewModel(appDb, prefs, dispatcherProvider) {
  val smsTemplates = appDb.smsTemplatesDao().loadAll()
}
