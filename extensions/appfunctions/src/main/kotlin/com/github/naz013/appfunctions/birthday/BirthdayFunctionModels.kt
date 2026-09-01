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

/** Identifies a single birthday for an update/delete action. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class BirthdayIdParams(
  /** The unique identifier of the birthday, as returned by another birthday AppFunction. */
  val id: String,
)

/** The parameters needed to update an existing birthday. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class UpdateBirthdayParams(
  /** The unique identifier of the birthday to update, as returned by another birthday AppFunction. */
  val id: String,
  /** The new name of the person whose birthday this is. */
  val name: String,
  /** The new date of birth. */
  val date: LocalDate,
  /** Whether to hide the birth year (show only day and month) when displaying this birthday. */
  val ignoreYear: Boolean = false,
)

/** The parameters needed to search existing birthdays by name. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SearchBirthdaysParams(
  /** The name text to search for. */
  val query: String,
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
