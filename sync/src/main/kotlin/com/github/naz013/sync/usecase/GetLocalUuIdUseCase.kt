package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Tag
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.model.NoteV3Json
import com.github.naz013.files.model.SettingsModel
import com.github.naz013.files.model.TagAssignmentsSnapshotJson
import com.github.naz013.sync.images.CachedFile

internal class GetLocalUuIdUseCase {
  operator fun invoke(any: Any): String {
    return when (any) {
      is ReminderV2 -> any.uuId
      is NoteWithImages -> any.note?.key ?: throw IllegalArgumentException("Note key is null")
      is Birthday -> any.uuId
      is GroupV2 -> any.uuId
      is Place -> any.id
      is SettingsModel -> "app"
      is TagAssignmentsSnapshotJson -> "app"
      is RecurPreset -> any.id
      is Tag -> any.id
      is CachedFile -> any.name
      is NoteV3Json -> any.key
      else -> throw IllegalArgumentException("Unsupported type: ${any::class.java}")
    }
  }
}
