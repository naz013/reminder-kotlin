package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.SmsBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType

class ActionDecomposer(
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val type = UiReminderType(reminder.type)
    val mainItem = when {
      type.isCall() -> {
        reminder.target.takeIf { it.isNotEmpty() }
          ?.let {
            biFactory.createWithValue(
              BiType.PHONE_CALL,
              it,
              PhoneCallBuilderItem::class.java
            )
          }
      }

      type.isSms() -> {
        reminder.target.takeIf { it.isNotEmpty() }
          ?.let { biFactory.createWithValue(BiType.SMS, it, SmsBuilderItem::class.java) }
      }

      type.isEmail() -> {
        reminder.target.takeIf { it.isNotEmpty() }
          ?.let { biFactory.createWithValue(BiType.EMAIL, it, EmailBuilderItem::class.java) }
      }

      type.isLink() -> {
        reminder.target.takeIf { it.isNotEmpty() }
          ?.let { biFactory.createWithValue(BiType.LINK, it, WebAddressBuilderItem::class.java) }
      }

      type.isApp() -> {
        reminder.target.takeIf { it.isNotEmpty() }
          ?.let {
            biFactory.createWithValue(BiType.APPLICATION, it, ApplicationBuilderItem::class.java)
          }
      }

      type.isSubTasks() -> {
        reminder.shoppings.takeIf { it.isNotEmpty() }
          ?.let {
            biFactory.createWithValue(BiType.SUB_TASKS, it, SubTasksBuilderItem::class.java)
          }
      }

      else -> null
    }
    return listOfNotNull(mainItem)
  }
}
