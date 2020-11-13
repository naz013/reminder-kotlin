package com.elementary.tasks.core.view_models.sms_templates

class SmsTemplatesViewModel : BaseSmsTemplatesViewModel() {
  val smsTemplates = appDb.smsTemplatesDao().loadAll()
}
