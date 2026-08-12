package com.github.naz013.sync.local

import com.github.naz013.files.DataType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagRepository

internal class DataTypeRepositoryCallerFactory(
  private val noteRepository: NoteRepository,
  private val birthdayRepository: BirthdayRepository,
  private val placeRepository: PlaceRepository,
  private val recurPresetRepository: RecurPresetRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val tagRepository: TagRepository
) {

  fun getCaller(dataType: DataType): DataTypeRepositoryCaller<*> {
    return when (dataType) {
      DataType.Reminders -> NoopRepositoryCaller()
      DataType.RemindersV2 -> ReminderV2RepositoryCaller(reminderV2Repository)
      DataType.Notes, DataType.NotesV2 -> NoteRepositoryCaller(noteRepository)
      DataType.Birthdays -> BirthdayRepositoryCaller(birthdayRepository)
      DataType.Groups -> NoopRepositoryCaller()
      DataType.GroupsV2 -> GroupV2RepositoryCaller(groupV2Repository)
      DataType.Places -> PlaceRepositoryCaller(placeRepository)
      DataType.Settings -> NoopRepositoryCaller()
      DataType.RecurPresets -> RecurPresetRepositoryCaller(recurPresetRepository)
      DataType.SharedNote -> NoopRepositoryCaller()
      DataType.Tags -> TagRepositoryCaller(tagRepository)
      DataType.TagAssignments -> NoopRepositoryCaller()
    }
  }
}
