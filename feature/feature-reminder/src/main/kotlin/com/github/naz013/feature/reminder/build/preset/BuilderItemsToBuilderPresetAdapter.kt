package com.github.naz013.feature.reminder.build.preset

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.domain.PresetBuilderScheme

class BuilderItemsToBuilderPresetAdapter(
  private val biValueToBuilderSchemeValue: BiValueToBuilderSchemeValue,
) {
  operator fun invoke(items: List<BuilderItem<*>>): List<PresetBuilderScheme> =
    items.mapIndexed { index, builderItem ->
      PresetBuilderScheme(
        type = builderItem.biType,
        position = index,
        value = biValueToBuilderSchemeValue(builderItem),
      )
    }
}
