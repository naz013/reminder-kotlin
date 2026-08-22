package com.github.naz013.localbackup.archive

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataConverter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * A minimal stand-in for the real [DataConverter] (internal to :files, so not reachable from
 * this module's tests) - a hand-rolled "tag|id|label" codec covering just the types these tests
 * exercise. This deliberately avoids running full domain objects (which have sealed-class fields
 * like RecurrenceRule) through reflection-based Gson, which needs a runtime type adapter per
 * sealed hierarchy to round-trip - exactly the complexity the real DataConverterImpl sidesteps by
 * mapping to a *Json wire model first. What's under test here is the archive's framing/bucketing,
 * not DataConverter's own (separately tested) JSON mapping.
 */
private class FakeDataConverter : DataConverter {
  override suspend fun toOutputStream(any: Any, outputStream: OutputStream) {
    val encoded = when (any) {
      is ReminderV2 -> "R|${any.uuId}|${any.summary}"
      is GroupV2 -> "G|${any.uuId}|${any.title}"
      is Birthday -> "B|${any.uuId}|${any.name}"
      is Tag -> "T|${any.id}|${any.name}"
      is TagAssignment -> "A|${any.tagId}|${any.itemId}::${any.itemType}"
      is Routine -> "O|${any.id}|${any.title}"
      is RoutineExecutionRecord -> "E|${any.id}|${any.routineId}"
      else -> error("FakeDataConverter does not support ${any::class.java}")
    }
    outputStream.use { it.write(encoded.toByteArray()) }
  }

  override suspend fun toInputStream(any: Any): InputStream {
    val buffer = ByteArrayOutputStream()
    toOutputStream(any, buffer)
    return ByteArrayInputStream(buffer.toByteArray())
  }

  override suspend fun toData(stream: InputStream): Any {
    val (tag, id, label) = stream.readBytes().decodeToString().split("|", limit = 3)
    return when (tag) {
      "R" -> ReminderV2(uuId = id, summary = label, schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))
      "G" -> GroupV2(uuId = id, title = label, createdAt = LocalDateTime.now(), syncState = SyncState.Synced)
      "B" -> Birthday(uuId = id, name = label, syncState = SyncState.Synced)
      "T" -> Tag(id = id, name = label, color = 0)
      "A" -> {
        val (itemId, itemType) = label.split("::", limit = 2)
        TagAssignment(tagId = id, itemId = itemId, itemType = TaggedItemType.valueOf(itemType))
      }
      "O" -> Routine(id = id, title = label, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
      "E" -> RoutineExecutionRecord(id = id, routineId = label, executedAt = LocalDateTime.now(), totalTimeSpentSeconds = 0, totalStepsCount = 0)
      else -> error("FakeDataConverter does not support tag $tag")
    }
  }
}

class BackupArchiveReaderWriterTest {

  private val dataConverter = FakeDataConverter()
  private val writer = BackupArchiveWriter(dataConverter)
  private val reader = BackupArchiveReader(dataConverter)

  private fun reminder(id: String) = ReminderV2(
    uuId = id,
    summary = "Reminder $id",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0)),
    notification = NotificationSettingsOverride()
  )

  private fun group(id: String) = GroupV2(
    uuId = id,
    title = "Group $id",
    createdAt = LocalDateTime.of(2026, 1, 1, 9, 0),
    syncState = SyncState.Synced
  )

  private fun birthday(id: String) = Birthday(uuId = id, name = "Birthday $id", syncState = SyncState.Synced)

  private fun tag(id: String) = Tag(id = id, name = "Tag $id", color = 0)

  private fun tagAssignment(tagId: String, itemId: String) =
    TagAssignment(tagId = tagId, itemId = itemId, itemType = TaggedItemType.NOTE)

  private fun routine(id: String) = Routine(
    id = id,
    title = "Routine $id",
    createdAt = LocalDateTime.of(2026, 1, 1, 9, 0),
    updatedAt = LocalDateTime.of(2026, 1, 1, 9, 0)
  )

  private fun routineExecution(id: String, routineId: String) = RoutineExecutionRecord(
    id = id,
    routineId = routineId,
    executedAt = LocalDateTime.of(2026, 1, 1, 9, 0),
    totalTimeSpentSeconds = 300,
    totalStepsCount = 3
  )

  @Test
  fun `round trips an empty envelope`() = runTest {
    val output = ByteArrayOutputStream()

    writer.write(output, BackupEnvelope())
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertTrue(result.isEmpty())
  }

  @Test
  fun `round trips reminders, groups and birthdays and buckets them by type`() = runTest {
    val envelope = BackupEnvelope(
      reminders = listOf(reminder("r1"), reminder("r2")),
      groups = listOf(group("g1")),
      birthdays = listOf(birthday("b1")),
    )
    val output = ByteArrayOutputStream()

    writer.write(output, envelope)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertEquals(2, result.reminders.size)
    assertEquals(setOf("r1", "r2"), result.reminders.map { it.uuId }.toSet())
    assertEquals(1, result.groups.size)
    assertEquals("g1", result.groups.single().uuId)
    assertEquals(1, result.birthdays.size)
    assertEquals("b1", result.birthdays.single().uuId)
    assertTrue(result.places.isEmpty())
    assertTrue(result.presets.isEmpty())
  }

  @Test
  fun `round trips tags and tag assignments and buckets them by type`() = runTest {
    val envelope = BackupEnvelope(
      tags = listOf(tag("t1"), tag("t2")),
      tagAssignments = listOf(tagAssignment("t1", "note-1"), tagAssignment("t2", "note-1")),
    )
    val output = ByteArrayOutputStream()

    writer.write(output, envelope)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertEquals(2, result.tags.size)
    assertEquals(setOf("t1", "t2"), result.tags.map { it.id }.toSet())
    assertEquals(2, result.tagAssignments.size)
    assertEquals(
      setOf("t1" to "note-1", "t2" to "note-1"),
      result.tagAssignments.map { it.tagId to it.itemId }.toSet()
    )
    assertTrue(result.reminders.isEmpty())
  }

  @Test
  fun `round trips routines and routine executions and buckets them by type`() = runTest {
    val envelope = BackupEnvelope(
      routines = listOf(routine("o1"), routine("o2")),
      routineExecutions = listOf(routineExecution("e1", "o1")),
    )
    val output = ByteArrayOutputStream()

    writer.write(output, envelope)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertEquals(2, result.routines.size)
    assertEquals(setOf("o1", "o2"), result.routines.map { it.id }.toSet())
    assertEquals(1, result.routineExecutions.size)
    assertEquals("o1", result.routineExecutions.single().routineId)
    assertTrue(result.reminders.isEmpty())
  }

  @Test
  fun `rejects an archive with an unrecognized format version`() = runTest {
    val output = ByteArrayOutputStream()
    writer.write(output, BackupEnvelope(reminders = listOf(reminder("r1"))))
    val bytes = output.toByteArray()
    bytes[3] = (bytes[3] + 1).toByte()

    var caught: UnsupportedBackupFormatException? = null
    try {
      reader.read(ByteArrayInputStream(bytes))
    } catch (e: UnsupportedBackupFormatException) {
      caught = e
    }

    assertNotNull(caught)
  }
}
