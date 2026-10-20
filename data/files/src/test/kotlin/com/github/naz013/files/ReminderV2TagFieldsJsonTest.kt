package com.github.naz013.files

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.model.ReminderV2Json
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Covers the `iconIndex`/`colorIndex` wire fields on [ReminderV2Json] through the same
 * [DataConverterImpl] mapping the backup and cloud sync use. `ReminderV2.toJson()` is private, so
 * it is reached reflectively, the same way [DataConverterImplTest] reaches its fix-up helpers.
 */
class ReminderV2TagFieldsJsonTest {

  private val gson = Gson()

  @Test
  fun `round trips a reminder icon and tag color index through the backup JSON`() {
    val reminder = taggedReminder(iconIndex = 5, colorIndex = 12)

    val json = gson.toJson(reminder.toJsonViaConverter())
    val result = gson.fromJson(json, ReminderV2Json::class.java).toDomainViaConverter()

    assertEquals(5, result.iconIndex)
    assertEquals(12, result.colorIndex)
    assertEquals(reminder.uuId, result.uuId)
  }

  @Test
  fun `writes the icon and color indices under their wire field names`() {
    val json = gson.toJson(taggedReminder(iconIndex = 2, colorIndex = 7).toJsonViaConverter())

    val wire = gson.fromJson(json, JsonObject::class.java)

    assertEquals(2, wire.get("iconIndex").asInt)
    assertEquals(7, wire.get("colorIndex").asInt)
  }

  @Test
  fun `round trips a reminder with no icon and no color as null`() {
    val reminder = taggedReminder(iconIndex = null, colorIndex = null)

    val json = gson.toJson(reminder.toJsonViaConverter())
    val result = gson.fromJson(json, ReminderV2Json::class.java).toDomainViaConverter()

    assertNull(result.iconIndex)
    assertNull(result.colorIndex)
  }

  @Test
  fun `reads an old backup JSON without the new fields as null icon and color`() {
    val legacyJson = gson.toJsonTree(taggedReminder(iconIndex = 1, colorIndex = 1).toJsonViaConverter())
      .asJsonObject
      .apply {
        remove("iconIndex")
        remove("colorIndex")
      }

    val result = gson.fromJson(legacyJson, ReminderV2Json::class.java).toDomainViaConverter()

    assertNull(result.iconIndex)
    assertNull(result.colorIndex)
    assertEquals("old-backup-id", result.uuId)
  }

  private fun taggedReminder(iconIndex: Int?, colorIndex: Int?) = ReminderV2(
    uuId = "old-backup-id",
    summary = "Water plants",
    recurrence = RecurrenceRule.Once,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
    action = ReminderAction.None,
    iconIndex = iconIndex,
    colorIndex = colorIndex,
  )

  private fun ReminderV2.toJsonViaConverter(): ReminderV2Json {
    val method = Class.forName("com.github.naz013.files.DataConverterImplKt")
      .getDeclaredMethod("toJson", ReminderV2::class.java)
    method.isAccessible = true
    return method.invoke(null, this) as ReminderV2Json
  }

  private fun ReminderV2Json.toDomainViaConverter(): ReminderV2 = toDomain() as ReminderV2
}
