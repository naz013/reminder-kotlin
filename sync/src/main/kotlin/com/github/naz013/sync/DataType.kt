package com.github.naz013.sync

enum class DataType(
  val fileExtension: String,
  val isLegacy: Boolean = false
) {
  Reminders(".ta2"),
  Notes(".no3"),
  Birthdays(".bi2"),
  Groups(".gr2"),
  Places(".pl2"),
  Settings(".settings"),
  RecurPresets(".rp2"),
  NotesV2(".no2", isLegacy = true),
}
