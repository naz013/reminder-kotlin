package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.sync.DataType
import com.github.naz013.sync.settings.SettingsModel

class GetClassByDataTypeUseCase {

  operator fun invoke(dataType: DataType): Class<*> {
    return when (dataType) {
      DataType.Reminders -> Reminder::class.java
      DataType.Notes -> OldNote::class.java
      DataType.Birthdays -> Birthday::class.java
      DataType.Groups -> ReminderGroup::class.java
      DataType.Places -> Place::class.java
      DataType.Settings -> SettingsModel::class.java
      DataType.RecurPresets -> RecurPreset::class.java
    }
  }
}
