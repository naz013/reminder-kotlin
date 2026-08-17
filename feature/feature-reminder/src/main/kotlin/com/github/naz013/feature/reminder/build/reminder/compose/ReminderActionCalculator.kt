package com.github.naz013.feature.reminder.build.reminder.compose

import com.github.naz013.feature.reminder.build.ApplicationBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.EmailBuilderItem
import com.github.naz013.feature.reminder.build.EmailSubjectBuilderItem
import com.github.naz013.feature.reminder.build.PhoneCallBuilderItem
import com.github.naz013.feature.reminder.build.SmsBuilderItem
import com.github.naz013.feature.reminder.build.WebAddressBuilderItem
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderAction

internal class ReminderActionCalculator {
  operator fun invoke(itemsMap: Map<BiType, BuilderItem<*>>): ReminderAction {
    val sms = (itemsMap[BiType.SMS] as? SmsBuilderItem)?.modifier?.takeIf { it.isCorrect() }?.getValue()
    if (sms != null) return ReminderAction.Sms(sms, "")

    val call = (itemsMap[BiType.PHONE_CALL] as? PhoneCallBuilderItem)?.modifier?.takeIf { it.isCorrect() }?.getValue()
    if (call != null) return ReminderAction.Call(call)

    val email = (itemsMap[BiType.EMAIL] as? EmailBuilderItem)?.modifier?.takeIf { it.isCorrect() }?.getValue()
    if (email != null) {
      val subject = (itemsMap[BiType.EMAIL_SUBJECT] as? EmailSubjectBuilderItem)?.modifier?.getValue() ?: ""
      return ReminderAction.Email(email, subject)
    }

    val link = (itemsMap[BiType.LINK] as? WebAddressBuilderItem)?.modifier?.takeIf { it.isCorrect() }?.getValue()
    if (link != null) return ReminderAction.Link(link)

    val app = (itemsMap[BiType.APPLICATION] as? ApplicationBuilderItem)?.modifier?.takeIf { it.isCorrect() }?.getValue()
    if (app != null) return ReminderAction.App(app)

    if (itemsMap[BiType.SUB_TASKS]?.modifier?.isCorrect() == true) return ReminderAction.Shopping

    return ReminderAction.None
  }
}
