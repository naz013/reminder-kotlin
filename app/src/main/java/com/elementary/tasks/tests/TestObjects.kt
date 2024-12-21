package com.elementary.tasks.tests

import android.text.TextUtils
import com.github.naz013.domain.Birthday
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

object TestObjects {

  const val ARG_TEST = "arg_test"
  const val ARG_TEST_HAS_NUMBER = "arg_test_has_number"

  fun getBirthday(number: String = ""): Birthday {
    val secKey = if (TextUtils.isEmpty(number)) "0" else number.substring(1)
    return Birthday(
      day = 25,
      month = 5,
      name = "Test User",
      showedYear = 2017,
      uniqueId = 12123,
      uuId = UUID.randomUUID().toString(),
      number = number,
      date = createBirthDate(25, 5, 1955),
      key = "Test User|$secKey",
      dayMonth = "25|5"
    )
  }

  private fun createBirthDate(day: Int, month: Int, year: Int): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
  }
}
