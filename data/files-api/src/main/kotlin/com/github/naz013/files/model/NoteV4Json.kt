package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

/** Backup/cloud-sync wire format for a note's unified, formattable content (see `NoteDocument`
 * in `core:domain`) - a structural mirror kept deliberately separate from the domain type rather
 * than reusing it directly, matching how every other note JSON DTO in this file is its own
 * shape. Supersedes [NoteV3Json]'s `summary`/`title`/`titleFontSize`/`titleFontStyle` split. */
data class NoteV4Json(
  @SerializedName("text")
  val text: String = "",
  @SerializedName("spans")
  val spans: List<NoteV4Span> = emptyList(),
  @SerializedName("key")
  val key: String = "",
  @SerializedName("date")
  val date: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("palette")
  val palette: Int = 0,
  @SerializedName("style")
  val style: Int = 0,
  @SerializedName("images")
  val images: List<NoteV3Image> = emptyList(),
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("uniqueId")
  val uniqueId: Int = 0,
  @SerializedName("fontSize")
  val fontSize: Int = -1,
  @SerializedName("archived")
  val archived: Boolean = false,
  @SerializedName("isPinned")
  val isPinned: Boolean = false,
  @SerializedName("versionId")
  var version: Long = 0L,
)

/** [type] is a discriminator naming the `NoteSpanAttribute` subtype (e.g. `"Bold"`,
 * `"FontFamily"`) - mirrors the `type`+payload-fields convention `DataConverterImpl` already
 * uses for other polymorphic domain types (e.g. `WorkflowTrigger`, `RecurrenceRule`), so
 * mapping to/from the sealed `NoteSpanAttribute` stays a plain `when` in one place rather than
 * needing a custom Gson type adapter shared across module boundaries. Only the field(s) that
 * subtype needs are populated. */
data class NoteV4Span(
  @SerializedName("start")
  val start: Int = 0,
  @SerializedName("end")
  val end: Int = 0,
  @SerializedName("type")
  val type: String = "",
  @SerializedName("intValue")
  val intValue: Int? = null,
  @SerializedName("colors")
  val colors: List<Int>? = null,
  @SerializedName("angleDegrees")
  val angleDegrees: Float? = null,
)
