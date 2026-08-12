package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.domain.PresetType

internal class PresetTypeConverter {

  @TypeConverter
  fun toInt(presetType: PresetType): Int {
    return presetType.ordinal
  }

  @TypeConverter
  fun toEnum(ordinal: Int): PresetType {
    return PresetType.entries[ordinal]
  }
}
