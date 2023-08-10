package com.elementary.tasks.core.utils.datetime.recurrence

sealed class Tag(
  open val tagType: TagType
) : Buildable

sealed class DateTimeTag(
  open val value: UtcDateTime,
  override val tagType: TagType
) : Tag(tagType)

data class DateTimeStampTag(
  override val value: UtcDateTime
) : DateTimeTag(value, TagType.DTSTAMP) {

  override fun buildString(): String {
    return "${tagType.value}:${value.buildString()}"
  }
}

data class DateTimeStartTag(
  override val value: UtcDateTime
) : DateTimeTag(value, TagType.DTSTART) {

  override fun buildString(): String {
    return "${tagType.value}:${value.buildString()}"
  }
}

data class DateTimeEndTag(
  override val value: UtcDateTime
) : DateTimeTag(value, TagType.DTEND) {

  override fun buildString(): String {
    return "${tagType.value}:${value.buildString()}"
  }
}

data class DurationTag(
  val duration: Duration
) : Tag(TagType.DURATION) {

  override fun buildString(): String {
    return "${tagType.value}:${duration.buildString()}"
  }
}

data class RepeatTag(
  val repeat: Int
) : Tag(TagType.REPEAT) {

  override fun buildString(): String {
    return "${tagType.value}:$repeat"
  }
}

data class VersionTag(
  val version: String
) : Tag(TagType.VERSION) {

  override fun buildString(): String {
    return "${tagType.value}:$version"
  }
}

data class ExceptionsDateTimeTag(
  val values: List<UtcDateTime>
) : Tag(TagType.EXDATE) {

  override fun buildString(): String {
    val value = values.joinToString(",") { it.buildString() }
    return "${tagType.value}:$value"
  }
}

data class RecurrenceDateTimeTag(
  val param: ValueParam?,
  val values: List<UtcDateTime>
) : Tag(TagType.RDATE) {

  override fun buildString(): String {
    val value = values.joinToString(",") { it.buildString() }
    val paramValue = if (param == null) {
      ""
    } else {
      ";${param.buildString()}"
    }
    return "${tagType.value}$paramValue:$value"
  }
}

data class RecurrenceRuleTag(
  val params: List<RecurParam>
) : Tag(TagType.RRULE) {

  fun hasCountParam(): Boolean {
    return params.firstOrNull { it.recurParamType == RecurParamType.COUNT }
      ?.let { it is CountRecurParam } ?: false
  }

  fun buildValueString(): String {
    return params.joinToString(";") { it.buildString() }
  }

  override fun buildString(): String {
    return "${tagType.value}:${buildValueString()}"
  }
}

enum class TagType(val value: String) {
  DTSTAMP("DTSTAMP"),
  DTSTART("DTSTART"),
  DTEND("DTEND"),

  DURATION("DURATION"),

  REPEAT("REPEAT"),

  EXDATE("EXDATE"),
  RDATE("RDATE"),

  RRULE("RRULE"),

  VERSION("VERSION")
}
