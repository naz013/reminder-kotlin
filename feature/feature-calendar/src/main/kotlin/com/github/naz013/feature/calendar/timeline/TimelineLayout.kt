package com.github.naz013.feature.calendar.timeline

import com.github.naz013.ui.agenda.UiAgendaItem

/** One event placed on the day's hour axis, with the lane it occupies among overlapping events. */
internal data class PositionedTimelineEvent(
  val item: UiAgendaItem,
  /** Minutes from midnight of the event's start, clamped to `0..MINUTES_IN_DAY`. */
  val startMinutes: Int,
  /** Zero-based column this event is drawn in within its overlap cluster. */
  val lane: Int,
  /** Total number of side-by-side lanes in this event's overlap cluster (>= 1). */
  val laneCount: Int,
)

/**
 * Lays out a single day's events onto side-by-side lanes so overlapping events don't visually
 * cover each other. Events are treated as fixed-height blocks of [blockMinutes] for the purpose
 * of deciding what overlaps (reminders/birthdays are point-in-time and carry no duration).
 *
 * Events are grouped into clusters of transitively-overlapping blocks; within a cluster each event
 * is greedily assigned to the first lane that has freed up by its start time, and every event in
 * the cluster is told the cluster's total lane count so the UI can split the column width evenly.
 * Pure and deterministic - unit-tested directly.
 */
internal fun layoutDayEvents(
  items: List<UiAgendaItem>,
  blockMinutes: Int,
): List<PositionedTimelineEvent> {
  if (items.isEmpty()) return emptyList()

  val sorted =
    items
      .map { it to (it.dateTime.hour * MINUTES_IN_HOUR + it.dateTime.minute).coerceIn(0, MINUTES_IN_DAY) }
      .sortedBy { it.second }

  val result = mutableListOf<PositionedTimelineEvent>()
  var cluster = mutableListOf<Pair<UiAgendaItem, Int>>()
  var clusterMaxEnd = Int.MIN_VALUE

  fun flushCluster() {
    if (cluster.isEmpty()) return
    val laneEnds = mutableListOf<Int>()
    val lanes = IntArray(cluster.size)
    cluster.forEachIndexed { index, (_, start) ->
      val freeLane = laneEnds.indexOfFirst { it <= start }
      val lane =
        if (freeLane >= 0) {
          laneEnds[freeLane] = start + blockMinutes
          freeLane
        } else {
          laneEnds.add(start + blockMinutes)
          laneEnds.lastIndex
        }
      lanes[index] = lane
    }
    val laneCount = laneEnds.size
    cluster.forEachIndexed { index, (item, start) ->
      result.add(PositionedTimelineEvent(item = item, startMinutes = start, lane = lanes[index], laneCount = laneCount))
    }
    cluster = mutableListOf()
    clusterMaxEnd = Int.MIN_VALUE
  }

  for ((item, start) in sorted) {
    if (cluster.isNotEmpty() && start >= clusterMaxEnd) {
      flushCluster()
    }
    cluster.add(item to start)
    clusterMaxEnd = maxOf(clusterMaxEnd, start + blockMinutes)
  }
  flushCluster()

  return result
}

internal const val MINUTES_IN_HOUR = 60
internal const val HOURS_IN_DAY = 24
internal const val MINUTES_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOUR
