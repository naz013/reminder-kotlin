package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.NoteV3Json
import com.github.naz013.sync.images.CachedFile
import com.github.naz013.sync.settings.SettingsModel

internal class GetLocalUuIdUseCase {
  operator fun invoke(any: Any): String {
    return when (any) {
      is Reminder -> any.uuId
      is NoteWithImages -> any.note?.key ?: throw IllegalArgumentException("Note key is null")
      is Birthday -> any.uuId
      is ReminderGroup -> any.groupUuId
      is Place -> any.id
      is SettingsModel -> "app"
      is RecurPreset -> any.id
      is CachedFile -> any.name
      is NoteV3Json -> any.key
      else -> throw IllegalArgumentException("Unsupported type: ${any::class.java}")
    }
  }
}
