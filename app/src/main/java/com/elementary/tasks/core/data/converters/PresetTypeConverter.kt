package com.elementary.tasks.core.data.converters

import androidx.room.TypeConverter
import com.elementary.tasks.core.data.models.PresetType

class PresetTypeConverter {

  @TypeConverter
  fun toInt(presetType: PresetType): Int {
    return presetType.ordinal
  }

  @TypeConverter
  fun toEnum(ordinal: Int): PresetType {
    return PresetType.entries[ordinal]
  }
}
