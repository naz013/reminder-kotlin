package com.github.naz013.sync

enum class DataType(
  val fileExtension: String
) {
  Reminders(".ta2"),
  Notes(".no2"),
  Birthdays(".bi2"),
  Groups(".gr2"),
  Places(".pl2"),
  Settings(".settings"),
  RecurPresets(".rp2")
}
