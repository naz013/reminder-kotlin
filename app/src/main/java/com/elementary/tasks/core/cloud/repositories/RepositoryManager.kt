package com.elementary.tasks.core.cloud.repositories

class RepositoryManager(
  val birthdayRepository: BirthdayRepository,
  val groupRepository: GroupRepository,
  val noteRepository: NoteRepository,
  val placeRepository: PlaceRepository,
  val reminderRepository: ReminderRepository,
  val settingsRepository: SettingsRepository,
  val templateRepository: TemplateRepository
)