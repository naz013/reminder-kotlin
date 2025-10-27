package com.github.naz013.sync.local

import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataType

internal class DataTypeRepositoryCallerFactory(
  private val reminderRepository: ReminderRepository,
  private val noteRepository: NoteRepository,
  private val birthdayRepository: BirthdayRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val placeRepository: PlaceRepository
) {

  fun getCaller(dataType: DataType): DataTypeRepositoryCaller<*> {
    return when (dataType) {
      DataType.Reminders -> ReminderRepositoryCaller(reminderRepository)
      DataType.Notes -> NoteRepositoryCaller(noteRepository)
      DataType.Birthdays -> BirthdayRepositoryCaller(birthdayRepository)
      DataType.Groups -> ReminderGroupRepositoryCaller(reminderGroupRepository)
      DataType.Places -> PlaceRepositoryCaller(placeRepository)
    }
  }
}
