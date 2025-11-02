package com.github.naz013.sync.usecase.download

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.NoteV3Json
import com.github.naz013.sync.DataType
import com.github.naz013.sync.settings.SettingsModel

internal class GetClassByDataTypeUseCase {

  operator fun invoke(dataType: DataType): Class<*> {
    return when (dataType) {
      DataType.Reminders -> Reminder::class.java
      DataType.Notes -> NoteV3Json::class.java
      DataType.Birthdays -> Birthday::class.java
      DataType.Groups -> ReminderGroup::class.java
      DataType.Places -> Place::class.java
      DataType.Settings -> SettingsModel::class.java
      DataType.RecurPresets -> RecurPreset::class.java
      DataType.NotesV2 -> OldNote::class.java
    }
  }
}
