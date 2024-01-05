package com.elementary.tasks.reminder.build.preset

import com.elementary.tasks.core.data.models.PresetBuilderScheme
import com.elementary.tasks.reminder.build.BuilderItem

class BuilderItemsToBuilderPresetAdapter(
  private val biValueToBuilderSchemeValue: BiValueToBuilderSchemeValue
) {

  operator fun invoke(items: List<BuilderItem<*>>): List<PresetBuilderScheme> {
    return items.mapIndexed { index, builderItem ->
      PresetBuilderScheme(
        type = builderItem.biType,
        position = index,
        value = biValueToBuilderSchemeValue(builderItem)
      )
    }
  }
}
