package com.github.naz013.appfunctions

/** AppFunction params/results use `java.time` (the type the AppFunctions framework marshals), while
 * the rest of the app - including [com.github.naz013.common.datetime.DateTimeManager] - uses the
 * org.threeten.bp backport. These convert at that boundary. */

fun java.time.LocalDateTime.toThreeTen(): org.threeten.bp.LocalDateTime =
  org.threeten.bp.LocalDateTime.of(year, monthValue, dayOfMonth, hour, minute, second)

fun org.threeten.bp.LocalDateTime.toJavaTime(): java.time.LocalDateTime =
  java.time.LocalDateTime.of(year, monthValue, dayOfMonth, hour, minute, second)

fun java.time.LocalDate.toThreeTen(): org.threeten.bp.LocalDate =
  org.threeten.bp.LocalDate.of(year, monthValue, dayOfMonth)

fun org.threeten.bp.LocalDate.toJavaTime(): java.time.LocalDate =
  java.time.LocalDate.of(year, monthValue, dayOfMonth)
