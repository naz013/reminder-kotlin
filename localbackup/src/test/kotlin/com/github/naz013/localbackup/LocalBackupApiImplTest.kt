package com.github.naz013.localbackup

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataConverter
import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/** See BackupArchiveReaderWriterTest for why a hand-rolled fake stands in for the real DataConverter. */
private class FakeDataConverter : DataConverter {
  override suspend fun toOutputStream(any: Any, outputStream: OutputStream) {
    val encoded = when (any) {
      is ReminderV2 -> "R|${any.uuId}|${any.summary}"
      is GroupV2 -> "G|${any.uuId}|${any.title}"
      is Birthday -> "B|${any.uuId}|${any.name}"
      is Tag -> "T|${any.id}|${any.name}"
      is TagAssignment -> "A|${any.tagId}|${any.itemId}::${any.itemType}"
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
      "T" -> Tag(id = id, name = label, color = 0, syncState = SyncState.Synced)
      "A" -> {
        val (itemId, itemType) = label.split("::", limit = 2)
        TagAssignment(tagId = id, itemId = itemId, itemType = TaggedItemType.valueOf(itemType))
      }
      else -> error("FakeDataConverter does not support tag $tag")
    }
  }
}

class LocalBackupApiImplTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>(relaxed = true)
  private val birthdayRepository = mockk<BirthdayRepository>(relaxed = true)
  private val placeRepository = mockk<PlaceRepository>(relaxed = true)
  private val recurPresetRepository = mockk<RecurPresetRepository>(relaxed = true)
  private val tagRepository = mockk<TagRepository>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)
  private val dataConverter = FakeDataConverter()

  private lateinit var api: LocalBackupApiImpl

  @Before
  fun setUp() {
    api = LocalBackupApiImpl(
      reminderV2Repository = reminderV2Repository,
      groupV2Repository = groupV2Repository,
      birthdayRepository = birthdayRepository,
      placeRepository = placeRepository,
      recurPresetRepository = recurPresetRepository,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      archiveWriter = BackupArchiveWriter(dataConverter),
      archiveReader = BackupArchiveReader(dataConverter)
    )
  }

  private fun reminder(id: String) = ReminderV2(
    uuId = id,
    summary = "Take pills",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
  )

  @Test
  fun `export reads every repository and writes a non-empty encrypted file`() = runTest {
    coEvery { reminderV2Repository.getAll() } returns listOf(reminder("r1"))
    val output = ByteArrayOutputStream()

    val result = api.export(output, "correct horse".toCharArray())

    assertTrue(result.isSuccess)
    coVerify { reminderV2Repository.getAll() }
    coVerify { groupV2Repository.getAll() }
    coVerify { birthdayRepository.getAll() }
    coVerify { placeRepository.getAll() }
    coVerify { recurPresetRepository.getAll() }
    coVerify { tagRepository.getAll() }
    coVerify { tagAssignmentRepository.getAll() }
    assertTrue(output.toByteArray().isNotEmpty())
  }

  @Test
  fun `export zeroes the passphrase array afterwards`() = runTest {
    val passphrase = "correct horse".toCharArray()

    api.export(ByteArrayOutputStream(), passphrase)

    assertTrue(passphrase.all { it == '0' })
  }

  @Test
  fun `round trips an export through import with the same passphrase`() = runTest {
    coEvery { reminderV2Repository.getAll() } returns listOf(reminder("r1"))
    coEvery { groupV2Repository.getAll() } returns listOf(
      GroupV2(uuId = "g1", title = "Work", createdAt = LocalDateTime.now(), syncState = SyncState.Synced)
    )
    val output = ByteArrayOutputStream()
    api.export(output, "correct horse".toCharArray())

    val result = api.import(ByteArrayInputStream(output.toByteArray()), "correct horse".toCharArray())

    assertTrue(result.isSuccess)
    val summary = result.getOrThrow()
    assertEquals(1, summary.remindersImported)
    assertEquals(1, summary.groupsImported)
    coVerify { reminderV2Repository.save(match { it.uuId == "r1" }) }
    coVerify { groupV2Repository.save(match { it.uuId == "g1" }) }
  }

  @Test
  fun `round trips tags and tag assignments through import`() = runTest {
    coEvery { tagRepository.getAll() } returns listOf(Tag(id = "t1", name = "Work", color = 1, syncState = SyncState.Synced))
    coEvery { tagAssignmentRepository.getAll() } returns listOf(
      TagAssignment(tagId = "t1", itemId = "r1", itemType = TaggedItemType.REMINDER)
    )
    val output = ByteArrayOutputStream()
    api.export(output, "correct horse".toCharArray())

    val result = api.import(ByteArrayInputStream(output.toByteArray()), "correct horse".toCharArray())

    assertTrue(result.isSuccess)
    val summary = result.getOrThrow()
    assertEquals(1, summary.tagsImported)
    assertEquals(1, summary.tagAssignmentsImported)
    coVerify { tagRepository.save(match { it.id == "t1" }) }
    coVerify { tagAssignmentRepository.replaceAll(match { it.size == 1 && it[0].tagId == "t1" }) }
  }

  @Test
  fun `import fails with WrongPassphraseException when the passphrase is wrong`() = runTest {
    coEvery { reminderV2Repository.getAll() } returns listOf(reminder("r1"))
    val output = ByteArrayOutputStream()
    api.export(output, "correct horse".toCharArray())

    val result = api.import(ByteArrayInputStream(output.toByteArray()), "wrong horse".toCharArray())

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is WrongPassphraseException)
  }

  @Test
  fun `import fails with InvalidBackupFileException for a file that is not a backup at all`() = runTest {
    val garbage = ByteArrayInputStream("not a backup file".toByteArray())

    val result = api.import(garbage, "any passphrase".toCharArray())

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is InvalidBackupFileException)
  }

  @Test
  fun `import zeroes the passphrase array afterwards`() = runTest {
    coEvery { reminderV2Repository.getAll() } returns listOf(reminder("r1"))
    val output = ByteArrayOutputStream()
    api.export(output, "correct horse".toCharArray())
    val importPassphrase = "correct horse".toCharArray()

    api.import(ByteArrayInputStream(output.toByteArray()), importPassphrase)

    assertTrue(importPassphrase.all { it == '0' })
  }
}
