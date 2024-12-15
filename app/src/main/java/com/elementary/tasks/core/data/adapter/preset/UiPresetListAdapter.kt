package com.elementary.tasks.core.data.adapter.preset

import com.elementary.tasks.core.data.adapter.UiAdapter
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.TagType

class UiPresetListAdapter(
  private val recurrenceManager: RecurrenceManager
) : UiAdapter<RecurPreset, UiPresetList> {

  override fun create(data: RecurPreset): UiPresetList {
    return UiPresetList(
      id = data.id,
      name = data.name,
      description = getDescription(data)
    )
  }

  private fun getDescription(data: RecurPreset): String {
    val rrule = runCatching { recurrenceManager.parseObject(data.recurObject) }
      .getOrNull()
      ?.map?.values?.firstOrNull { it.tagType == TagType.RRULE }
      ?.buildString()
    return rrule ?: data.description ?: ""
  }
}
