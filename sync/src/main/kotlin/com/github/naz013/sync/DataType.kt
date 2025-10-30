package com.github.naz013.sync

enum class DataType(
  val fileExtension: String
) {
  Reminders(".ta2"),
  Notes(".no2"),
  Birthdays(".gr2"),
  Groups(".bi2"),
  Places(".pl2"),
  Settings(".settings")
}
