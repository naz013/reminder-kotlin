package com.github.naz013.localbackup

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.DataConverter
import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.localbackup.archive.BackupEnvelope
import io.mockk.coEvery
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
      else -> error("FakeDataConverter does not support tag $tag")
    }
  }
}

class LocalBackupApiImplTest {

  private val buildBackupEnvelopeUseCase = mockk<BuildBackupEnvelopeUseCase>()
  private val applyBackupEnvelopeUseCase = mockk<ApplyBackupEnvelopeUseCase>()
  private val dataConverter = FakeDataConverter()

  private lateinit var api: LocalBackupApiImpl

  @Before
  fun setUp() {
    api = LocalBackupApiImpl(
      buildBackupEnvelopeUseCase = buildBackupEnvelopeUseCase,
      applyBackupEnvelopeUseCase = applyBackupEnvelopeUseCase,
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
  fun `export builds the envelope and writes a non-empty encrypted file`() = runTest {
    coEvery { buildBackupEnvelopeUseCase() } returns BackupEnvelope(reminders = listOf(reminder("r1")))
    val output = ByteArrayOutputStream()

    val result = api.export(output, "correct horse".toCharArray())

    assertTrue(result.isSuccess)
    assertTrue(output.toByteArray().isNotEmpty())
  }

  @Test
  fun `export zeroes the passphrase array afterwards`() = runTest {
    coEvery { buildBackupEnvelopeUseCase() } returns BackupEnvelope()
    val passphrase = "correct horse".toCharArray()

    api.export(ByteArrayOutputStream(), passphrase)

    assertTrue(passphrase.all { it == '0' })
  }

  @Test
  fun `round trips an export through import with the same passphrase`() = runTest {
    coEvery { buildBackupEnvelopeUseCase() } returns BackupEnvelope(reminders = listOf(reminder("r1")))
    coEvery { applyBackupEnvelopeUseCase(any()) } returns ImportSummary(
      remindersImported = 1,
      groupsImported = 0,
      birthdaysImported = 0,
      placesImported = 0,
      presetsImported = 0,
      tagsImported = 0,
      tagAssignmentsImported = 0
    )
    val output = ByteArrayOutputStream()
    api.export(output, "correct horse".toCharArray())

    val result = api.import(ByteArrayInputStream(output.toByteArray()), "correct horse".toCharArray())

    assertTrue(result.isSuccess)
    assertEquals(1, result.getOrThrow().remindersImported)
  }

  @Test
  fun `import fails with WrongPassphraseException when the passphrase is wrong`() = runTest {
    coEvery { buildBackupEnvelopeUseCase() } returns BackupEnvelope(reminders = listOf(reminder("r1")))
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
    coEvery { buildBackupEnvelopeUseCase() } returns BackupEnvelope(reminders = listOf(reminder("r1")))
    coEvery { applyBackupEnvelopeUseCase(any()) } returns ImportSummary(
      remindersImported = 1,
      groupsImported = 0,
      birthdaysImported = 0,
      placesImported = 0,
      presetsImported = 0,
      tagsImported = 0,
      tagAssignmentsImported = 0
    )
    val output = ByteArrayOutputStream()
    api.export(output, "correct horse".toCharArray())
    val importPassphrase = "correct horse".toCharArray()

    api.import(ByteArrayInputStream(output.toByteArray()), importPassphrase)

    assertTrue(importPassphrase.all { it == '0' })
  }
}
