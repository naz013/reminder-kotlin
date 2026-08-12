package com.github.naz013.datecalc

class DateValidator {
  fun isLegacyMonthValid(month: Int): Boolean = month in 0..11

  fun isMonthValid(month: Int): Boolean = month in 1..12
}
