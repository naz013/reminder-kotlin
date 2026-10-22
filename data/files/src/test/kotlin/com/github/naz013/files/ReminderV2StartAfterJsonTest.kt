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

/** Covers the `startAfterReminderId` wire field on [ReminderV2Json] through [DataConverterImpl]. */
class ReminderV2StartAfterJsonTest {

  private val gson = Gson()

  @Test
  fun `round trips the start after reminder link through the backup JSON`() {
    val reminder = reminder(startAfterReminderId = "first-id")

    val json = gson.toJson(reminder.toJsonViaConverter())
    val result = gson.fromJson(json, ReminderV2Json::class.java).toDomainViaConverter()

    assertEquals("first-id", result.startAfterReminderId)
    assertEquals(reminder.uuId, result.uuId)
  }

  @Test
  fun `writes the link under its wire field name`() {
    val json = gson.toJson(reminder(startAfterReminderId = "first-id").toJsonViaConverter())

    val wire = gson.fromJson(json, JsonObject::class.java)

    assertEquals("first-id", wire.get("startAfterReminderId").asString)
  }

  @Test
  fun `reads an old backup JSON without the link as null`() {
    val legacyJson = gson.toJsonTree(reminder(startAfterReminderId = "x").toJsonViaConverter())
      .asJsonObject
      .apply { remove("startAfterReminderId") }

    val result = gson.fromJson(legacyJson, ReminderV2Json::class.java).toDomainViaConverter()

    assertNull(result.startAfterReminderId)
    assertEquals("chained-id", result.uuId)
  }

  private fun reminder(startAfterReminderId: String?) = ReminderV2(
    uuId = "chained-id",
    summary = "Second step",
    recurrence = RecurrenceRule.Once,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
    action = ReminderAction.None,
    startAfterReminderId = startAfterReminderId,
  )

  private fun ReminderV2.toJsonViaConverter(): ReminderV2Json {
    val method = Class.forName("com.github.naz013.files.DataConverterImplKt")
      .getDeclaredMethod("toJson", ReminderV2::class.java)
    method.isAccessible = true
    return method.invoke(null, this) as ReminderV2Json
  }

  private fun ReminderV2Json.toDomainViaConverter(): ReminderV2 = toDomain() as ReminderV2
}
