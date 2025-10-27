package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.NoteWithImages

internal class GetLocalUuIdUseCase {
  operator fun invoke(any: Any): String {
    return when (any) {
      is Reminder -> any.uuId
      is NoteWithImages -> any.note?.key ?: throw IllegalArgumentException("Note key is null")
      is Birthday -> any.uuId
      is ReminderGroup -> any.groupUuId
      is Place -> any.id
      else -> throw IllegalArgumentException("Unsupported type: ${any::class.java}")
    }
  }
}
