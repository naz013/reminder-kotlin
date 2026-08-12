package com.github.naz013.domain.reminder.migration

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

/**
 * One-time V1 ReminderGroup -> GroupV2 backfill mapper. Preserves [groupUuId] as
 * [GroupV2.uuId] so existing ReminderV2.groupId references (copied from V1 Reminder.groupUuId by
 * `Reminder.toReminderV2`) keep resolving to the same group after both backfills have run.
 */
fun ReminderGroup.toGroupV2(): GroupV2 = GroupV2(
  uuId = groupUuId,
  title = groupTitle,
  color = groupColor,
  isDefault = isDefaultGroup,
  notification = NotificationSettingsOverride(),
  createdAt = parseGmtToUtc(groupDateTime) ?: LocalDateTime.now(ZoneOffset.UTC),
  version = version,
  syncState = syncState
)

/**
 * Reverse of [toGroupV2] - used where a real, non-Room V1 [ReminderGroup] object is still needed
 * on demand (e.g. the local share/export-file format), now that [GroupV2] is the write-of-record.
 */
fun GroupV2.toReminderGroup(): ReminderGroup = ReminderGroup(
  groupTitle = title,
  groupUuId = uuId,
  groupColor = color,
  groupDateTime = createdAt.atZone(ZoneOffset.UTC).format(GROUP_GMT_FORMATTER),
  isDefaultGroup = isDefault,
  version = version,
  syncState = syncState
)

private val GROUP_GMT_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)

private fun parseGmtToUtc(value: String?): LocalDateTime? {
  if (value.isNullOrEmpty()) return null
  return runCatching {
    ZonedDateTime.parse(value, GROUP_GMT_FORMATTER).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
  }.getOrNull()
}
