package com.elementary.tasks.reminder.build.adapter

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.BuilderItem

class BiValueForUiAdapter(
  private val context: Context
) {

  fun getUiRepresentation(item: BuilderItem<*>): String {
    val emptyText = when (item.biType) {
      BiType.SUMMARY -> ""
      BiType.DESCRIPTION -> ""
      else -> context.getString(R.string.builder_not_selected)
    }
    return item.modifier.getUiRepresentation(emptyText)
  }
}
