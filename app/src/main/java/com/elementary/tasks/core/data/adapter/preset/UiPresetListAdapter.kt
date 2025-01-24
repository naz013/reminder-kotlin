package com.elementary.tasks.core.data.adapter.preset

import com.elementary.tasks.core.data.adapter.UiAdapter
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.github.naz013.domain.RecurPreset
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.TagType

class UiPresetListAdapter(
  private val iCalendarApi: ICalendarApi
) : UiAdapter<RecurPreset, UiPresetList> {

  override fun create(data: RecurPreset): UiPresetList {
    return UiPresetList(
      id = data.id,
      name = data.name,
      description = getDescription(data)
    )
  }

  private fun getDescription(data: RecurPreset): String {
    val rrule = runCatching { iCalendarApi.parseObject(data.recurObject) }
      .getOrNull()
      ?.map?.values?.firstOrNull { it.tagType == TagType.RRULE }
      ?.buildString()
    return rrule ?: data.description ?: ""
  }
}
