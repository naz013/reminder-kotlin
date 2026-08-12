package com.github.naz013.datecalc

import java.util.Locale

interface DateTimePreferences {
  val is24HourFormat: Boolean
  val birthdayTime: String
  val locale: Locale
}
