package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.view_models.DispatcherProvider

class SmsTemplatesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider
) : BaseSmsTemplatesViewModel(appDb, prefs, dispatcherProvider, workManagerProvider) {
  val smsTemplates = appDb.smsTemplatesDao().loadAll()
}
