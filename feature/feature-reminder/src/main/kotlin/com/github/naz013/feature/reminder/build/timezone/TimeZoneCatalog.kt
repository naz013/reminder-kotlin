package com.github.naz013.feature.reminder.build.timezone

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.util.Locale

/** One row of the zone picker; a null [id] is the device (floating) option. */
internal data class UiTimeZoneOption(
  val id: String?,
  val title: String,
  val offset: String,
)

internal class TimeZoneCatalog(
  private val deviceLabel: String,
  private val availableIds: () -> Set<String> = { ZoneId.getAvailableZoneIds() },
  private val nowProvider: () -> Instant = { Instant.now() },
) {
  private val zones: List<UiTimeZoneOption> by lazy {
    val now = nowProvider()
    availableIds()
      .filter { isPickable(it) }
      .mapNotNull { id -> runCatching { ZoneId.of(id) }.getOrNull()?.let { toOption(id, it, now) } }
      .sortedWith(compareBy({ offsetSeconds(it.id) }, { it.title }))
  }

  /** The device option first, then zones whose name or offset contains every word of [query]. */
  fun options(query: String): List<UiTimeZoneOption> {
    val deviceOption = UiTimeZoneOption(id = null, title = deviceLabel, offset = "")
    val words = query.trim().lowercase(Locale.ROOT).split(WHITESPACE).filter { it.isNotEmpty() }
    if (words.isEmpty()) return listOf(deviceOption) + zones
    val matching = zones.filter { option -> words.all { option.searchText().contains(it) } }
    val deviceMatches = words.all { deviceLabel.lowercase(Locale.ROOT).contains(it) }
    return if (deviceMatches) listOf(deviceOption) + matching else matching
  }

  /** "Europe/Berlin (UTC+01:00)", or the device label for a null or unknown id. */
  fun label(id: String?): String {
    val zone = id?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: return deviceLabel
    val option = toOption(id, zone, nowProvider())
    return "${option.title} (${option.offset})"
  }

  fun offsetLabel(zone: ZoneId): String = formatOffset(zone.rules.getOffset(nowProvider()).totalSeconds)

  private fun toOption(
    id: String,
    zone: ZoneId,
    now: Instant,
  ) = UiTimeZoneOption(
    id = id,
    title = id.replace('_', ' '),
    offset = formatOffset(zone.rules.getOffset(now).totalSeconds),
  )

  private fun offsetSeconds(id: String?): Int =
    id?.let { runCatching { ZoneId.of(it).rules.getOffset(nowProvider()).totalSeconds }.getOrNull() } ?: 0

  private fun UiTimeZoneOption.searchText(): String = "$title $offset".lowercase(Locale.ROOT)

  private fun isPickable(id: String): Boolean =
    id == UTC || (id.contains('/') && IGNORED_PREFIXES.none { id.startsWith(it) })

  private fun formatOffset(totalSeconds: Int): String {
    val sign = if (totalSeconds < 0) '-' else '+'
    val absolute = kotlin.math.abs(totalSeconds)
    val hours = absolute / SECONDS_IN_HOUR
    val minutes = absolute % SECONDS_IN_HOUR / SECONDS_IN_MINUTE
    return String.format(Locale.ROOT, "UTC%c%02d:%02d", sign, hours, minutes)
  }

  private companion object {
    const val UTC = "UTC"
    const val SECONDS_IN_HOUR = 3600
    const val SECONDS_IN_MINUTE = 60
    val WHITESPACE = Regex("\\s+")
    val IGNORED_PREFIXES = listOf("Etc/", "SystemV/", "US/", "Brazil/", "Canada/", "Chile/", "Mexico/")
  }
}
