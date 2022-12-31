package com.elementary.tasks.core.data.adapter.sms

import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.data.ui.sms.UiSmsList

class UiSmsListAdapter {

  fun convert(smsTemplate: SmsTemplate): UiSmsList {
    return UiSmsList(
      id = smsTemplate.key,
      text = smsTemplate.title
    )
  }
}
