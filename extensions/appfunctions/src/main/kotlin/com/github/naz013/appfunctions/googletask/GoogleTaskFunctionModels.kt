package com.github.naz013.appfunctions.googletask

import androidx.appfunctions.AppFunctionSerializable
import java.time.LocalDateTime

/** The parameters needed to create a new Google Task in the default task list. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CreateGoogleTaskParams(
  /** The title of the task. */
  val title: String,
  /** Optional notes to attach to the task. */
  val notes: String? = null,
  /** Optional date and time, in the device's local time zone, the task is due at. */
  val dueDateTime: LocalDateTime? = null,
)

/** The parameters needed to look up existing Google Tasks. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ListGoogleTasksParams(
  /** Whether to include tasks that are already marked complete. Defaults to false. */
  val includeCompleted: Boolean = false,
)

/** Identifies a single Google Task for a complete action. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class GoogleTaskIdParams(
  /** The unique identifier of the task, as returned by another Google Task AppFunction. */
  val id: String,
)

/** The parameters needed to update an existing Google Task. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class UpdateGoogleTaskParams(
  /** The unique identifier of the task to update, as returned by another Google Task AppFunction. */
  val id: String,
  /** The new title of the task. */
  val title: String,
  /** Optional new notes for the task. */
  val notes: String? = null,
  /** Optional new date and time, in the device's local time zone, the task is due at. */
  val dueDateTime: LocalDateTime? = null,
)

/** The parameters needed to search existing Google Tasks. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SearchGoogleTasksParams(
  /** The text to search for within task titles and notes. */
  val query: String,
)

/** A Google Task. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class GoogleTaskFunctionResult(
  /** The unique identifier of the task. */
  val id: String,
  /** The title of the task. */
  val title: String,
  /** Notes attached to the task, if any. */
  val notes: String?,
  /** The date and time, in the device's local time zone, the task is due at, if any. */
  val dueDateTime: LocalDateTime?,
  /** Whether the task is marked complete. */
  val isCompleted: Boolean,
)
