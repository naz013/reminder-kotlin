package com.elementary.tasks.reminder.build.preset

import com.github.naz013.domain.PresetBuilderScheme
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
