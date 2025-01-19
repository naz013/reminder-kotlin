package com.github.naz013.domain.reminder

data class ReminderType(
  val value: Int
) {

  constructor(base: Base, kind: Kind) : this(
    base.value + kind.value
  )

  fun hasCallAction(): Boolean = isKind(Kind.CALL)

  fun hasSmsAction(): Boolean = isKind(Kind.SMS)

  fun hasApplicationAction(): Boolean = isKind(Kind.APP)

  fun hasLinkAction(): Boolean = isKind(Kind.LINK)

  fun hasSubTasks(): Boolean = isKind(Kind.SHOPPING)

  fun hasEmailAction(): Boolean = isKind(Kind.EMAIL)

  fun isDateTime(): Boolean = baseOf(Base.DATE)

  fun isCountdown(): Boolean = baseOf(Base.TIMER)

  fun isByDayOfWeek(): Boolean = baseOf(Base.WEEKDAY)

  fun isByDayOfMonth(): Boolean = baseOf(Base.MONTHLY)

  fun isByDayOfYear(): Boolean = baseOf(Base.YEARLY)

  fun isICalendar(): Boolean = baseOf(Base.ICALENDAR)

  private fun isKind(kind: Kind): Boolean {
    return value % Base.DATE.value == kind.value
  }

  fun isSameAs(type: Int): Boolean {
    return value == type
  }

  fun baseOf(base: Base): Boolean {
    val res = value - base.value
    return res in 0..9
  }

  fun isGpsType(): Boolean {
    return baseOf(Base.LOCATION_IN) || baseOf(Base.LOCATION_OUT) || baseOf(Base.PLACE)
  }

  enum class Base(val value: Int) {
    DATE(10),
    TIMER(20),
    WEEKDAY(30),
    LOCATION_IN(40),

    @Suppress("unused")
    @Deprecated("This type is removed from application")
    SKYPE(50),
    MONTHLY(60),
    LOCATION_OUT(70),

    @Deprecated("This type is removed from application")
    PLACE(80),
    YEARLY(90),
    ICALENDAR(100)
  }

  enum class Kind(val value: Int) {
    CALL(1),
    SMS(2),

    @Deprecated("This type is not supported in newer OS versions")
    APP(3),
    LINK(4),
    SHOPPING(5),
    EMAIL(6)
  }
}
