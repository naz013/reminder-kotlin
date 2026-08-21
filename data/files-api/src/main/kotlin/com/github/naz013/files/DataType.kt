package com.github.naz013.files

enum class DataType(
  val fileExtension: String,
  val isLegacy: Boolean = false
) {
  Reminders(".ta2", isLegacy = true),
  RemindersV2(".ta3"),
  Notes(".no3"),
  Birthdays(".bi2"),
  Groups(".gr2", isLegacy = true),
  GroupsV2(".gr3"),
  Places(".pl2"),
  Settings(".settings"),
  RecurPresets(".rp2"),
  NotesV2(".no2", isLegacy = true),
  SharedNote(".etnote"),
  Tags(".tg1"),
  TagAssignments(".tga1"),
  Routines(".rt1")
}
