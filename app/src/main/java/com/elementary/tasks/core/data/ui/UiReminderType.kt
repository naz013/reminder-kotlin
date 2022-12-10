package com.elementary.tasks.core.data.ui

data class UiReminderType(
  val value: Int
) {

  constructor(base: Base, kind: Kind): this(
    base.value + kind.value
  )

  fun isCall() = isKind(Kind.CALL)

  fun isSms() = isKind(Kind.SMS)

  fun isApp() = isKind(Kind.APP)

  fun isLink() = isKind(Kind.LINK)

  fun isShopping() = isKind(Kind.SHOPPING)

  fun isEmail() = isKind(Kind.EMAIL)

  fun isByDate() = isBase(Base.DATE)

  fun isTimer() = isBase(Base.TIMER)

  fun isByWeekday() = isBase(Base.WEEKDAY)

  fun isMonthly() = isBase(Base.MONTHLY)

  fun isYearly() = isBase(Base.YEARLY)

  private fun isKind(kind: Kind): Boolean {
    return value % Base.DATE.value == kind.value
  }

  fun isSame(base: Int): Boolean {
    return value == base
  }

  fun isBase(base: Base): Boolean {
    val res = value - base.value
    return res in 0..9
  }

  fun isGpsType(): Boolean {
    return isBase(Base.LOCATION_IN) || isBase(Base.LOCATION_OUT) || isBase(Base.PLACE)
  }

  enum class Base(val value: Int) {
    DATE(10),
    TIMER(20),
    WEEKDAY(30),
    LOCATION_IN(40),
    SKYPE(50),
    MONTHLY(60),
    LOCATION_OUT(70),
    PLACE(80),
    YEARLY(90)
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