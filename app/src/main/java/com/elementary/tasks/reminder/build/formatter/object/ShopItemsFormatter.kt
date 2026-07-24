package com.elementary.tasks.reminder.build.formatter.`object`

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.reminder.v2.ShopItemV2

class ShopItemsFormatter(
  private val context: Context,
) : Formatter<List<ShopItem>>() {
  override fun format(items: List<ShopItem>): String =
    if (items.isEmpty()) {
      context.getString(R.string.shopping_list_is_empty)
    } else {
      buildString(items.map { Triple(it.summary, it.isChecked, it.isDeleted) })
    }

  fun formatV2(items: List<ShopItemV2>): String =
    if (items.isEmpty()) {
      context.getString(R.string.shopping_list_is_empty)
    } else {
      buildString(items.map { Triple(it.summary, it.isChecked, it.isDeleted) })
    }

  private fun buildString(items: List<Triple<String, Boolean, Boolean>>): String {
    val numberOfDeletedItems =
      items.filter { it.third }.size.takeIf { it > 0 }?.let {
        context.resources.getQuantityString(R.plurals.x_items_deleted, it, it)
      }
    val mappedItems =
      items
        .filterNot { it.third }
        .map { asString(summary = it.first, isChecked = it.second) }
    return (mappedItems + numberOfDeletedItems)
      .filterNotNull()
      .joinToString(separator = "\n") { it }
  }

  private fun asString(
    summary: String,
    isChecked: Boolean,
  ): String = "${getCheckMark(isChecked)} $summary"

  private fun getCheckMark(isChecked: Boolean): String =
    if (isChecked) {
      "☑"
    } else {
      "☐"
    }
}
