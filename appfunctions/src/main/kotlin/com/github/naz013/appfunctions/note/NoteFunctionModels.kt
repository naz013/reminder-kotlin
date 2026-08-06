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
