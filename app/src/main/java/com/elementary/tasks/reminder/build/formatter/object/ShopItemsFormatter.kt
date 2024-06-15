package com.elementary.tasks.reminder.build.formatter.`object`

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.reminder.build.formatter.Formatter

class ShopItemsFormatter(
  private val context: Context
) : Formatter<List<ShopItem>>() {

  override fun format(items: List<ShopItem>): String {
    return if (items.isEmpty()) {
      context.getString(R.string.shopping_list_is_empty)
    } else {
      buildString(items)
    }
  }

  private fun buildString(items: List<ShopItem>): String {
    val numberOfDeletedItems = items.filter { it.isDeleted }.size.takeIf { it > 0 }?.let {
      context.resources.getQuantityString(R.plurals.x_items_deleted, it, it)
    }
    val mappedItems = items.filterNot { it.isDeleted }
      .map { it.asString() }
    return (mappedItems + numberOfDeletedItems)
      .filterNotNull()
      .joinToString(separator = "\n") { it }
  }

  private fun ShopItem.asString(): String {
    return "${getCheckMark()} $summary"
  }

  private fun ShopItem.getCheckMark(): String {
    return if (isChecked) {
      "☑"
    } else {
      "☐"
    }
  }
}
