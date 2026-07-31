package com.github.naz013.appfunctions.birthday

import androidx.appfunctions.AppFunctionSerializable
import java.time.LocalDate

/** The parameters needed to create a new birthday. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CreateBirthdayParams(
  /** The name of the person whose birthday this is. */
  val name: String,
  /** The date of birth. */
  val date: LocalDate,
  /** Whether to hide the birth year (show only day and month) when displaying this birthday. */
  val ignoreYear: Boolean = false,
)

/** The parameters needed to look for birthdays coming up in the near future. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ListUpcomingBirthdaysParams(
  /** How many days from now, inclusive, to look for upcoming birthdays in. Defaults to 30. */
  val withinDays: Int = 30,
)

/** A birthday. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class BirthdayFunctionResult(
  /** The unique identifier of the birthday. */
  val id: String,
  /** The name of the person. */
  val name: String,
  /** The date of birth. */
  val date: LocalDate,
)
