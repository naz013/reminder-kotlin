package com.elementary.tasks.reminder.build.bi

import com.elementary.tasks.reminder.build.bi.constraint.constraints
import com.github.naz013.domain.reminder.BiType
import org.junit.Assert.assertEquals
import org.junit.Test

class BuilderItemConstraintsTest {

  @Test
  fun `test requireAll extraction`() {
    val constraints = constraints {
      requiresAll(
        BiType.ARRIVING_COORDINATES,
        BiType.DATE
      )
      requiresAll(BiGroup.ICAL)
      requiresAll(BiType.BEFORE_TIME)
      requiresAny(BiType.DAYS_OF_WEEK)
    }

    val subject = BuilderItemConstraints(constraints)

    assertEquals(4, subject.requiresAll.size)
    assertEquals(3, subject.requiresAllType.size)
    assertEquals(1, subject.requiresAllGroup.size)
  }
}
