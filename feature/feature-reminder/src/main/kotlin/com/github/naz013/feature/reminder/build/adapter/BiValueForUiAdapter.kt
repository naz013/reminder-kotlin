package com.github.naz013.feature.reminder.build.adapter

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.domain.reminder.BiType

class BiValueForUiAdapter(
  private val context: Context,
) {
  fun getUiRepresentation(item: BuilderItem<*>): String {
    val emptyText =
      when (item.biType) {
        BiType.SUMMARY -> ""
        BiType.DESCRIPTION -> ""
        else -> context.getString(R.string.builder_not_selected)
      }
    return item.modifier.getUiRepresentation(emptyText)
  }
}
