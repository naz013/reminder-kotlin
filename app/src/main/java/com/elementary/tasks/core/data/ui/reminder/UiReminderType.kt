package com.elementary.tasks.core.data.ui.reminder

import com.github.naz013.analytics.AnalyticsReminderType

data class UiReminderType(
  val value: Int
) {

  constructor(base: Base, kind: Kind) : this(
    base.value + kind.value
  )

  fun isCall(): Boolean = isKind(Kind.CALL)

  fun isSms(): Boolean = isKind(Kind.SMS)

  fun isApp(): Boolean = isKind(Kind.APP)

  fun isLink(): Boolean = isKind(Kind.LINK)

  fun isSubTasks(): Boolean = isKind(Kind.SHOPPING)

  fun isEmail(): Boolean = isKind(Kind.EMAIL)

  fun isByDate(): Boolean = isBase(Base.DATE)

  fun isTimer(): Boolean = isBase(Base.TIMER)

  fun isByWeekday(): Boolean = isBase(Base.WEEKDAY)

  fun isMonthly(): Boolean = isBase(Base.MONTHLY)

  fun isYearly(): Boolean = isBase(Base.YEARLY)

  fun isRecur(): Boolean = isBase(Base.RECUR)

  private fun isKind(kind: Kind): Boolean {
    return value % Base.DATE.value == kind.value
  }

  fun isSame(type: Int): Boolean {
    return value == type
  }

  fun isBase(base: Base): Boolean {
    val res = value - base.value
    return res in 0..9
  }

  fun isGpsType(): Boolean {
    return isBase(Base.LOCATION_IN) || isBase(Base.LOCATION_OUT) || isBase(Base.PLACE)
  }

  fun getEventType(): AnalyticsReminderType {
    return when {
      isRecur() -> AnalyticsReminderType.Recur
      isEmail() -> AnalyticsReminderType.Email
      isLink() -> AnalyticsReminderType.WebLink
      isApp() -> AnalyticsReminderType.App
      isCall() -> AnalyticsReminderType.Call
      isSms() -> AnalyticsReminderType.Sms
      isGpsType() -> AnalyticsReminderType.Gps
      isMonthly() -> AnalyticsReminderType.Monthly
      isByWeekday() -> AnalyticsReminderType.Weekday
      isTimer() -> AnalyticsReminderType.Timer
      isYearly() -> AnalyticsReminderType.Yearly
      isByDate() -> AnalyticsReminderType.ByDate
      else -> AnalyticsReminderType.Other
    }
  }

  enum class Base(val value: Int) {
    DATE(10),
    TIMER(20),
    WEEKDAY(30),
    LOCATION_IN(40),

    @Deprecated("This type is removed from application")
    SKYPE(50),
    MONTHLY(60),
    LOCATION_OUT(70),

    @Deprecated("This type is removed from application")
    PLACE(80),
    YEARLY(90),
    RECUR(100)
  }

  enum class Kind(val value: Int) {
    CALL(1),
    SMS(2),
    APP(3),
    LINK(4),
    SHOPPING(5),
    EMAIL(6)
  }
}
