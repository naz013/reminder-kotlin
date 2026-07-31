package com.github.naz013.appfunctions.reminder

import androidx.appfunctions.AppFunctionSerializable
import java.time.LocalDateTime

/** The parameters needed to create a new one-time reminder. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CreateReminderParams(
  /** The short title of the reminder, for example "Pay rent" or "Call the dentist". */
  val title: String,
  /** The date and time, in the device's local time zone, the reminder should trigger at. */
  val dueDateTime: LocalDateTime,
  /** Optional free-text notes to attach to the reminder. */
  val notes: String? = null,
)

/** The parameters needed to look for reminders due in the near future. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ListUpcomingRemindersParams(
  /** How many days from now, inclusive, to look for due reminders in. Defaults to 7. */
  val withinDays: Int = 7,
)

/** Identifies a single reminder for a complete/delete action. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ReminderIdParams(
  /** The unique identifier of the reminder, as returned by another reminder AppFunction. */
  val id: String,
)

/** A reminder. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ReminderFunctionResult(
  /** The unique identifier of the reminder. */
  val id: String,
  /** The title of the reminder. */
  val title: String,
  /** The date and time, in the device's local time zone, the reminder will trigger at. */
  val dueDateTime: LocalDateTime,
)
