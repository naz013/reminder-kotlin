package com.elementary.tasks.core.view_models.sms_templates

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs

class SmsTemplatesViewModel(
  appDb: AppDb,
  prefs: Prefs
) : BaseSmsTemplatesViewModel(appDb, prefs) {
  val smsTemplates = appDb.smsTemplatesDao().loadAll()
}
