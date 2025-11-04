package com.github.naz013.cloudapi

object FileConfig {
  /**
   * File extensions for reminder.
   */
  const val FILE_NAME_REMINDER = ".ta2"

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
  const val FILE_NAME_GROUP = ".gr2"

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
