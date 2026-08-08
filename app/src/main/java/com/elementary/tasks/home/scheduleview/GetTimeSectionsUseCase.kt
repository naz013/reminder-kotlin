package com.elementary.tasks.home.scheduleview

import com.elementary.tasks.home.HomeEvent
import com.elementary.tasks.home.TimeSection
import com.github.naz013.datecalc.DateTimeManager

class GetTimeSectionsUseCase(
  private val dateTimeManager: DateTimeManager,
) {
  operator fun invoke(events: List<HomeEvent>): List<TimeSection> {
    if (events.isEmpty()) return emptyList()
    val timeSections = mutableListOf<TimeSection>()
    var previousTime =
      events.first().let {
        timeSections.add(
          TimeSection(
            time = dateTimeManager.getTime(it.time),
            event = it,
          ),
        )
        it.time
      }
    for (i in 1 until events.size) {
      val event = events[i]
      val time = event.time
      if (time.hour != previousTime.hour) {
        timeSections.add(
          TimeSection(
            time = dateTimeManager.getTime(time),
            event = event,
          ),
        )
        previousTime = time
      } else {
        timeSections.add(
          TimeSection(
            time = "",
            event = event,
          ),
        )
      }
    }
    return timeSections
  }
}
