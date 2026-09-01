package com.github.naz013.appfunctions.note

import androidx.appfunctions.AppFunctionSerializable

/** The parameters needed to create a new note. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CreateNoteParams(
  /** The title of the note. */
  val title: String,
  /** The body text of the note. */
  val content: String,
)

/** The parameters needed to search existing notes. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SearchNotesParams(
  /** The text to search for within note titles and content. */
  val query: String,
)

/** Identifies a single note for an update/delete action. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class NoteIdParams(
  /** The unique identifier of the note, as returned by another note AppFunction. */
  val id: String,
)

/** The parameters needed to update an existing note. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class UpdateNoteParams(
  /** The unique identifier of the note to update, as returned by another note AppFunction. */
  val id: String,
  /** The new title of the note. */
  val title: String,
  /** The new body text of the note. */
  val content: String,
)

/** A note. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class NoteFunctionResult(
  /** The unique identifier of the note. */
  val id: String,
  /** The title of the note. */
  val title: String,
  /** The body text of the note. */
  val content: String,
)
