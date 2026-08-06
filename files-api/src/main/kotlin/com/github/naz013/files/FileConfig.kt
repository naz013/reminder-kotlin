package com.github.naz013.files

object FileConfig {
  /**
   * File extensions for reminder.
   */
  @Deprecated("Use FILE_NAME_REMINDER_V2 instead")
  const val FILE_NAME_REMINDER = ".ta2"
  const val FILE_NAME_REMINDER_V2 = ".ta3"

  /**
   * File extension for note.
   */
  @Deprecated("Use FILE_NAME_NOTE_V3 instead")
  const val FILE_NAME_NOTE = ".no2"
  const val FILE_NAME_NOTE_V3 = ".no3"
  const val FILE_NAME_NOTE_IMAGE = ".nif"

  /**
   * File extension for reminder reminderGroup.
   */
  @Deprecated("Use FILE_NAME_GROUP_V2 instead")
  const val FILE_NAME_GROUP = ".gr2"
  const val FILE_NAME_GROUP_V2 = ".gr3"

  /**
   * File extension for birthday.
   */
  const val FILE_NAME_BIRTHDAY = ".bi2"

  /**
   * File extension for place.
   */
  const val FILE_NAME_PLACE = ".pl2"

  /**
   * File extension for preset.
   */
  const val FILE_NAME_PRESET = ".rp2"

  const val FILE_NAME_SETTINGS = "app.settings"
  const val FILE_NAME_SETTINGS_EXT = ".settings"

  const val FILE_NAME_JSON = ".json"
}
