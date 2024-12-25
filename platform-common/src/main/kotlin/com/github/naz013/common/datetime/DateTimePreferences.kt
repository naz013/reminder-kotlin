package com.github.naz013.common.datetime

import java.util.Locale

interface DateTimePreferences {
  val is24HourFormat: Boolean
  val birthdayTime: String
  val locale: Locale
}
