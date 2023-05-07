package com.elementary.tasks.core.cloud.converters

class ConverterManager(
  val birthdayConverter: BirthdayConverter,
  val groupConverter: GroupConverter,
  val noteConverter: NoteConverter,
  val placeConverter: PlaceConverter,
  val reminderConverter: ReminderConverter,
  val settingsConverter: SettingsConverter
)
