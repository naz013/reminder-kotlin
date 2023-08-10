package com.elementary.tasks.core.utils.datetime.recurrence

sealed class RecurParam(
  val recurParamType: RecurParamType
) : Buildable {

  abstract fun buildValue(): String?

  override fun buildString(): String {
    val value = buildValue() ?: return ""
    return "${recurParamType.value}=$value"
  }
}

data class CountRecurParam(
  val value: Int
) : RecurParam(RecurParamType.COUNT) {

  override fun buildValue(): String {
    return value.toString()
  }
}

data class IntervalRecurParam(
  val value: Int
) : RecurParam(RecurParamType.INTERVAL) {

  override fun buildValue(): String {
    return value.toString()
  }
}

data class FreqRecurParam(
  val value: FreqType
) : RecurParam(RecurParamType.FREQ) {

  override fun buildValue(): String {
    return value.value
  }
}

data class UntilRecurParam(
  val value: UtcDateTime
) : RecurParam(RecurParamType.UNTIL) {

  override fun buildValue(): String {
    return value.buildString()
  }
}

data class ByMonthRecurParam(
  val value: List<Int> // Valid values are 1 to 12
) : RecurParam(RecurParamType.BYMONTH) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class ByDayRecurParam(
  val value: List<DayValue>
) : RecurParam(RecurParamType.BYDAY) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.buildString() }
  }
}

data class WeekStartRecurParam(
  val value: DayValue
) : RecurParam(RecurParamType.WEEKSTART) {

  override fun buildValue(): String {
    return value.buildString()
  }
}

data class ByMonthDayRecurParam(
  val value: List<Int> // Valid values are 1 to 31 or -31 to -1
) : RecurParam(RecurParamType.BYMONTHDAY) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class ByHourRecurParam(
  val value: List<Int> // Valid values are 0 to 23
) : RecurParam(RecurParamType.BYHOUR) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class ByMinuteRecurParam(
  val value: List<Int> // Valid values are 0 to 59
) : RecurParam(RecurParamType.BYMINUTE) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class ByYearDayRecurParam(
  val value: List<Int> // Valid values are 1 to 366 or -366 to -1
) : RecurParam(RecurParamType.BYYEARDAY) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class ByWeekNumberRecurParam(
  val value: List<Int> // Valid values are 1 to 53 or -53 to -1
) : RecurParam(RecurParamType.BYWEEKNO) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class BySetPosRecurParam(
  val value: List<Int> // Valid values are 1 to 366 or -366 to -1
) : RecurParam(RecurParamType.BYSETPOS) {

  override fun buildValue(): String? {
    return value.takeIf { it.isNotEmpty() }
      ?.joinToString(",") { it.toString() }
  }
}

data class DayValue(val value: String) : Buildable {

  var isDefault: Boolean = false
    private set
  var hasPrefix: Boolean = false
    private set
  var day: Day? = null
    private set

  init {
    validateDay()
    Day.values().firstOrNull { it.value == value }?.also {
      isDefault = true
      day = it
    }
    hasPrefix = hasPrefix(value)
  }

  constructor(day: Day) : this(day.value)

  override fun buildString(): String {
    return value
  }

  private fun validateDay() {
    if (!containsDay(value)) {
      throw IllegalArgumentException("Should contain one of: SU,MO,TU,WE,TH,FR,SA, but was $value")
    }
  }

  private fun containsDay(value: String): Boolean {
    return Day.values().map { it.value }.any { value.contains(it) }
  }

  private fun hasPrefix(value: String): Boolean {
    return Day.values().map { it.value }.firstOrNull { value.contains(it) }?.let { day ->
      val withoutDay = value.replace(day, "")
      if (withoutDay.isEmpty()) {
        false
      } else {
        val integer = runCatching { withoutDay.toInt() }.getOrNull()
        integer != null
      }
    } ?: false
  }
}

enum class FreqType(val value: String) {
  YEARLY("YEARLY"),
  MONTHLY("MONTHLY"),
  WEEKLY("WEEKLY"),
  DAILY("DAILY"),
  HOURLY("HOURLY"),
  MINUTELY("MINUTELY")
}

enum class RecurParamType(val value: String) {
  FREQ("FREQ"), // YEARLY,MONTHLY,WEEKLY,DAILY,HOURLY,MINUTELY
  INTERVAL("INTERVAL"), // Interval for FREQ
  COUNT("COUNT"), // number of occurrences
  UNTIL("UNTIL"), // Date time
  BYMONTH("BYMONTH"), // 6,7, values 1-12
  BYDAY("BYDAY"), // SU,MO,TU,WE,TH,FR,SA, 20MO - 20th monday
  BYMONTHDAY("BYMONTHDAY"), // 15,30,-1 - last day, -3 - third-to-the-last day of the month
  BYHOUR("BYHOUR"), // 9,10,11,12,13,14,15,16
  BYMINUTE("BYMINUTE"), // 0,20,40
  BYYEARDAY("BYYEARDAY"), // 1,100,200
  BYWEEKNO("BYWEEKNO"), // 20
  WEEKSTART("WKST"), // MO, TU, WE, TH, FR, SA, and SU
  BYSETPOS("BYSETPOS") // 1 to 366 or -366 to -1
}

enum class Day(val value: String) {
  SU("SU"), // Sunday
  MO("MO"), // Monday
  TU("TU"), // Tuesday
  WE("WE"), // Wednesday
  TH("TH"), // Thursday
  FR("FR"), // Friday
  SA("SA") // Saturday
}
