package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.SmsBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2

class ActionDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val mainItem =
      when (val action = reminder.action) {
        is ReminderAction.Call -> {
          action.target
            .takeIf { it.isNotEmpty() }
            ?.let { biFactory.createWithValue(BiType.PHONE_CALL, it, PhoneCallBuilderItem::class.java) }
        }

        is ReminderAction.Sms -> {
          action.target
            .takeIf { it.isNotEmpty() }
            ?.let { biFactory.createWithValue(BiType.SMS, it, SmsBuilderItem::class.java) }
        }

        is ReminderAction.Email -> {
          action.target
            .takeIf { it.isNotEmpty() }
            ?.let { biFactory.createWithValue(BiType.EMAIL, it, EmailBuilderItem::class.java) }
        }

        is ReminderAction.Link -> {
          action.target
            .takeIf { it.isNotEmpty() }
            ?.let { biFactory.createWithValue(BiType.LINK, it, WebAddressBuilderItem::class.java) }
        }

        is ReminderAction.App -> {
          action.target
            .takeIf { it.isNotEmpty() }
            ?.let { biFactory.createWithValue(BiType.APPLICATION, it, ApplicationBuilderItem::class.java) }
        }

        is ReminderAction.Shopping -> {
          reminder.shoppingItems
            .takeIf { it.isNotEmpty() }
            ?.map { it.toShopItem() }
            ?.let { biFactory.createWithValue(BiType.SUB_TASKS, it, SubTasksBuilderItem::class.java) }
        }

        ReminderAction.None -> null
      }
    return listOfNotNull(mainItem)
  }

  /** [SubTasksBuilderItem]/its formatter and modifier are still typed against V1's [ShopItem],
   * so [ShopItem.createTime] is round-tripped through the same UTC->local->GMT-string path the
   * reverse mapper uses. The composer's `ShopItemsModifier.toShopItemV2()` is this function's
   * exact inverse. */
  private fun ShopItemV2.toShopItem(): ShopItem = ShopItem(
    summary = summary,
    isDeleted = isDeleted,
    isChecked = isChecked,
    uuId = uuId,
    createTime = dateTimeManager.getGmtFromDateTime(dateTimeManager.utcToLocal(createdAt)),
  )
}
