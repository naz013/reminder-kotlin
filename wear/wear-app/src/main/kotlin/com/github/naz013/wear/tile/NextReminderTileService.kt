package com.github.naz013.wear.tile

import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.tile
import com.github.naz013.wear.R
import com.github.naz013.wear.sync.WearReminderRepository
import com.github.naz013.wearsync.WearReminderSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Glanceable "next reminder" tile - a lighter-weight surface than opening [com.github.naz013.wear.MainActivity]
 * for the common case of just checking what's coming up next. Read-only: tapping a reminder to
 * complete/snooze still happens in the app itself. */
internal class NextReminderTileService : Material3TileService() {

  override suspend fun MaterialScope.tileResponse(requestParams: TileRequest): Tile {
    WearReminderRepository.refreshFromDataClient(this@NextReminderTileService)
    val next = WearReminderRepository.reminders.value.firstOrNull()
    return tile(Timeline.fromLayoutElement(tileLayout(next)))
  }

  private fun MaterialScope.tileLayout(reminder: WearReminderSummary?): LayoutElement =
    primaryLayout(
      titleSlot = { text(getString(R.string.wear_app_name).layoutString) },
      mainSlot = { text(mainText(reminder).layoutString) },
    )

  private fun mainText(reminder: WearReminderSummary?): String {
    if (reminder == null) return getString(R.string.wear_empty_state)
    val due = formatDueTime(reminder.eventDateTimeMillis)
    return if (due != null) "${reminder.title} · $due" else reminder.title
  }

  private fun formatDueTime(eventDateTimeMillis: Long?): String? {
    if (eventDateTimeMillis == null) return null
    val dateTime = Instant.ofEpochMilli(eventDateTimeMillis).atZone(ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("EEE, HH:mm"))
  }
}
