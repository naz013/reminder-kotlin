package com.elementary.tasks.core.cloud.repositories

class RepositoryManager(
  val birthdayDataFlowRepository: BirthdayDataFlowRepository,
  val groupDataFlowRepository: GroupDataFlowRepository,
  val noteDataFlowRepository: NoteDataFlowRepository,
  val placeDataFlowRepository: PlaceDataFlowRepository,
  val reminderDataFlowRepository: ReminderDataFlowRepository,
  val settingsDataFlowRepository: SettingsDataFlowRepository
)
