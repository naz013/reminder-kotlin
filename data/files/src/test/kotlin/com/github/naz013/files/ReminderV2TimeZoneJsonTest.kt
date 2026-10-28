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

/** Covers the `timeZoneId` wire field on [ReminderV2Json] through [DataConverterImpl]. */
class ReminderV2TimeZoneJsonTest {

  private val gson = Gson()

  @Test
  fun `round trips the time zone id through the backup JSON`() {
    val reminder = reminder(timeZoneId = "Europe/Berlin")

    val json = gson.toJson(reminder.toJsonViaConverter())
    val result = gson.fromJson(json, ReminderV2Json::class.java).toDomainViaConverter()

    assertEquals("Europe/Berlin", result.timeZoneId)
  }

  @Test
  fun `writes the time zone under its wire field name`() {
    val json = gson.toJson(reminder(timeZoneId = "Asia/Tokyo").toJsonViaConverter())

    val wire = gson.fromJson(json, JsonObject::class.java)

    assertEquals("Asia/Tokyo", wire.get("timeZoneId").asString)
  }

  @Test
  fun `keeps a floating reminder floating`() {
    val json = gson.toJson(reminder(timeZoneId = null).toJsonViaConverter())

    val result = gson.fromJson(json, ReminderV2Json::class.java).toDomainViaConverter()

    assertNull(result.timeZoneId)
  }

  @Test
  fun `reads an old backup JSON without the field as floating`() {
    val legacyJson = gson.toJsonTree(reminder(timeZoneId = "Europe/Berlin").toJsonViaConverter())
      .asJsonObject
      .apply { remove("timeZoneId") }

    val result = gson.fromJson(legacyJson, ReminderV2Json::class.java).toDomainViaConverter()

    assertNull(result.timeZoneId)
    assertEquals("zoned-id", result.uuId)
  }

  @Test
  fun `reads an invalid zone string as floating instead of throwing`() {
    val json = gson.toJsonTree(reminder(timeZoneId = "Europe/Berlin").toJsonViaConverter())
      .asJsonObject
      .apply { addProperty("timeZoneId", "Mars/Olympus_Mons") }

    val result = gson.fromJson(json, ReminderV2Json::class.java).toDomainViaConverter()

    assertNull(result.timeZoneId)
    assertEquals("zoned-id", result.uuId)
  }

  private fun reminder(timeZoneId: String?) = ReminderV2(
    uuId = "zoned-id",
    summary = "Call Berlin",
    recurrence = RecurrenceRule.Once,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
    action = ReminderAction.None,
    timeZoneId = timeZoneId,
  )

  private fun ReminderV2.toJsonViaConverter(): ReminderV2Json {
    val method = Class.forName("com.github.naz013.files.DataConverterImplKt")
      .getDeclaredMethod("toJson", ReminderV2::class.java)
    method.isAccessible = true
    return method.invoke(null, this) as ReminderV2Json
  }

  private fun ReminderV2Json.toDomainViaConverter(): ReminderV2 = toDomain() as ReminderV2
}
