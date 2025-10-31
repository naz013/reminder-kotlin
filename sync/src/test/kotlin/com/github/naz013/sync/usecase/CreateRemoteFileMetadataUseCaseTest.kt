package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.sync.settings.SettingsModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [CreateRemoteFileMetadataUseCase].
 *
 * Tests the creation of RemoteFileMetadata objects from CloudFile and domain entities,
 * ensuring proper mapping of all fields and correct extraction of local UUIDs.
 */
class CreateRemoteFileMetadataUseCaseTest {

  private lateinit var getLocalUuIdUseCase: GetLocalUuIdUseCase
  private lateinit var createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase

  @Before
  fun setUp() {
    getLocalUuIdUseCase = mockk()
    createRemoteFileMetadataUseCase = CreateRemoteFileMetadataUseCase(
      getLocalUuIdUseCase = getLocalUuIdUseCase
    )
  }

  @Test
  fun invoke_withReminderAndCloudFile_shouldCreateRemoteFileMetadataWithAllFields() {
    // Arrange - Create a reminder with a cloud file from Google Drive
    val reminderUuId = "reminder-uuid-12345"
    val reminder = Reminder(
      summary = "Doctor appointment",
      uuId = reminderUuId,
      groupUuId = "default-group",
      eventTime = "2025-10-30 14:00",
      reminderType = 0
    )
    val cloudFile = CloudFile(
      id = "gdrive-file-id-abc123",
      name = "$reminderUuId.ta2",
      fileExtension = ".ta2",
      lastModified = 1698765432000L,
      size = 2048,
      version = 3L,
      rev = "revision-xyz"
    )
    val source = "GDRIVE"

    every { getLocalUuIdUseCase(reminder) } returns reminderUuId

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, reminder)

    // Assert - All fields should be properly mapped
    assertNotNull(result)
    assertEquals("gdrive-file-id-abc123", result.id)
    assertEquals("$reminderUuId.ta2", result.name)
    assertEquals(1698765432000L, result.lastModified)
    assertEquals(2048, result.size)
    assertEquals("GDRIVE", result.source)
    assertEquals(reminderUuId, result.localUuId)
    assertEquals(".ta2", result.fileExtension)
    assertEquals(3L, result.version)
    assertEquals("revision-xyz", result.rev)
    verify(exactly = 1) { getLocalUuIdUseCase(reminder) }
  }

  @Test
  fun invoke_withBirthdayFromDropbox_shouldCreateMetadataWithDropboxSource() {
    // Arrange - Birthday entity uploaded to Dropbox
    val birthdayUuId = "birthday-uuid-67890"
    val birthday = Birthday(
      name = "John Doe",
      uuId = birthdayUuId,
      date = "1990-05-15",
      day = 15,
      month = 5,
      showedYear = 1990,
      syncState = SyncState.Synced
    )
    val cloudFile = CloudFile(
      id = "dropbox-file-id-def456",
      name = "$birthdayUuId.bi2",
      fileExtension = ".bi2",
      lastModified = 1698800000000L,
      size = 1024,
      version = 1L,
      rev = "rev-dropbox-1"
    )
    val source = "DROPBOX"

    every { getLocalUuIdUseCase(birthday) } returns birthdayUuId

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, birthday)

    // Assert
    assertEquals("DROPBOX", result.source)
    assertEquals(birthdayUuId, result.localUuId)
    assertEquals(".bi2", result.fileExtension)
    assertEquals("dropbox-file-id-def456", result.id)
  }

  @Test
  fun invoke_withNoteAndEmptyCloudFileId_shouldCreateMetadataWithEmptyId() {
    // Arrange - New note that hasn't been uploaded before
    val noteKey = "note-key-abc123"
    val note = OldNote(
      summary = "Meeting notes",
      key = noteKey,
      date = "2025-10-29",
      color = 0xFF0000,
      uniqueId = 12345
    )
    val cloudFile = CloudFile(
      id = "",  // Empty ID for new file
      name = "$noteKey.no2",
      fileExtension = ".no2",
      lastModified = 0L,
      size = 0,
      version = 0L,
      rev = ""
    )
    val source = "GDRIVE"

    every { getLocalUuIdUseCase(note) } returns noteKey

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, note)

    // Assert - Should handle empty values gracefully
    assertEquals("", result.id)
    assertEquals(noteKey, result.localUuId)
    assertEquals(0L, result.lastModified)
    assertEquals(0, result.size)
    assertEquals(0L, result.version)
    assertEquals("", result.rev)
  }

  @Test
  fun invoke_withReminderGroupAndLargeFile_shouldPreserveFileSize() {
    // Arrange - Large reminder group file
    val groupUuId = "group-uuid-xyz789"
    val reminderGroup = ReminderGroup(
      groupTitle = "Work Reminders",
      groupUuId = groupUuId,
      groupColor = 0x00FF00,
      groupDateTime = "2025-10-29 10:00",
      isDefaultGroup = false,
      syncState = SyncState.Synced
    )
    val largeFileSize = 10485760  // 10 MB
    val cloudFile = CloudFile(
      id = "large-file-id-123",
      name = "$groupUuId.gr2",
      fileExtension = ".gr2",
      lastModified = 1698850000000L,
      size = largeFileSize,
      version = 5L,
      rev = "large-rev-5"
    )
    val source = "GDRIVE"

    every { getLocalUuIdUseCase(reminderGroup) } returns groupUuId

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, reminderGroup)

    // Assert - Large file size should be preserved accurately
    assertEquals(largeFileSize, result.size)
    assertEquals(groupUuId, result.localUuId)
    assertEquals(5L, result.version)
  }

  @Test
  fun invoke_withPlaceFromMultipleSources_shouldDifferentiateBySource() {
    // Arrange - Same place uploaded to different cloud providers
    val placeId = "place-id-location-456"
    val place = Place(
      id = placeId,
      name = "Home",
      latitude = 40.7128,
      longitude = -74.0060,
      radius = 100,
      address = "123 Main St, New York, NY",
      marker = 1,
      syncState = SyncState.Synced
    )
    val gdriveFile = CloudFile(
      id = "gdrive-place-id",
      name = "$placeId.pl2",
      fileExtension = ".pl2",
      lastModified = 1698900000000L,
      size = 512,
      version = 2L,
      rev = "gdrive-rev-2"
    )
    val dropboxFile = CloudFile(
      id = "dropbox-place-id",
      name = "$placeId.pl2",
      fileExtension = ".pl2",
      lastModified = 1698900001000L,
      size = 512,
      version = 2L,
      rev = "dropbox-rev-2"
    )

    every { getLocalUuIdUseCase(place) } returns placeId

    // Act - Create metadata for both sources
    val gdriveMetadata = createRemoteFileMetadataUseCase("GDRIVE", gdriveFile, place)
    val dropboxMetadata = createRemoteFileMetadataUseCase("DROPBOX", dropboxFile, place)

    // Assert - Same entity, different sources and IDs
    assertEquals("GDRIVE", gdriveMetadata.source)
    assertEquals("gdrive-place-id", gdriveMetadata.id)
    assertEquals(placeId, gdriveMetadata.localUuId)

    assertEquals("DROPBOX", dropboxMetadata.source)
    assertEquals("dropbox-place-id", dropboxMetadata.id)
    assertEquals(placeId, dropboxMetadata.localUuId)

    // LocalUuId should be the same for both
    assertEquals(gdriveMetadata.localUuId, dropboxMetadata.localUuId)
  }

  @Test
  fun invoke_withSettingsModel_shouldExtractCorrectLocalUuId() {
    // Arrange - Settings with app-level configuration
    val settingsUuId = "settings-app-uuid"
    val settingsModel = SettingsModel(
      mapOf(
        "theme" to "dark",
        "language" to "en",
        "notifications" to true
      )
    )
    val cloudFile = CloudFile(
      id = "settings-cloud-id",
      name = "app.settings",
      fileExtension = ".settings",
      lastModified = 1698950000000L,
      size = 256,
      version = 10L,
      rev = "settings-rev-10"
    )
    val source = "GDRIVE"

    every { getLocalUuIdUseCase(settingsModel) } returns settingsUuId

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, settingsModel)

    // Assert - Settings should have correct UUID extraction
    assertEquals(settingsUuId, result.localUuId)
    assertEquals(".settings", result.fileExtension)
    assertEquals("app.settings", result.name)
    assertEquals(10L, result.version)
  }

  @Test
  fun invoke_withMaximumVersionNumber_shouldPreserveVersionValue() {
    // Arrange - File with high version number (after many syncs)
    val reminderUuId = "reminder-versioned-uuid"
    val reminder = Reminder(
      summary = "Frequently updated reminder",
      uuId = reminderUuId,
      groupUuId = "group1",
      eventTime = "2025-11-01 09:00",
      reminderType = 1
    )
    val highVersion = 999999L
    val cloudFile = CloudFile(
      id = "high-version-file-id",
      name = "$reminderUuId.ta2",
      fileExtension = ".ta2",
      lastModified = 1699000000000L,
      size = 3072,
      version = highVersion,
      rev = "rev-999999"
    )
    val source = "GDRIVE"

    every { getLocalUuIdUseCase(reminder) } returns reminderUuId

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, reminder)

    // Assert - High version number should be preserved
    assertEquals(highVersion, result.version)
    assertEquals("rev-999999", result.rev)
    assertEquals(reminderUuId, result.localUuId)
  }

  @Test
  fun invoke_withSpecialCharactersInRevision_shouldPreserveRevisionString() {
    // Arrange - CloudFile with complex revision string (Dropbox style)
    val birthdayUuId = "birthday-special-uuid"
    val birthday = Birthday(
      name = "Jane Smith",
      uuId = birthdayUuId,
      date = "1985-03-20",
      day = 20,
      month = 3,
      syncState = SyncState.Synced
    )
    val complexRev = "ZjRlMzY4YTAtYWJjZC0xMjM0LTU2NzgtOTBhYmNkZWYwMTIz"  // Base64-like
    val cloudFile = CloudFile(
      id = "complex-rev-file-id",
      name = "$birthdayUuId.bi2",
      fileExtension = ".bi2",
      lastModified = 1699050000000L,
      size = 768,
      version = 7L,
      rev = complexRev
    )
    val source = "DROPBOX"

    every { getLocalUuIdUseCase(birthday) } returns birthdayUuId

    // Act
    val result = createRemoteFileMetadataUseCase(source, cloudFile, birthday)

    // Assert - Complex revision string should be preserved exactly
    assertEquals(complexRev, result.rev)
    assertEquals("DROPBOX", result.source)
    assertEquals(birthdayUuId, result.localUuId)
  }
}
