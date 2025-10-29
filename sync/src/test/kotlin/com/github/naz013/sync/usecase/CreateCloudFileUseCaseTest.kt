package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.settings.SettingsModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [CreateCloudFileUseCase].
 *
 * Tests the creation of CloudFile objects from various domain entities,
 * including file naming, metadata retrieval, and error handling.
 */
class CreateCloudFileUseCaseTest {

  private lateinit var getLocalUuIdUseCase: GetLocalUuIdUseCase
  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var createCloudFileUseCase: CreateCloudFileUseCase

  @Before
  fun setUp() {
    getLocalUuIdUseCase = mockk()
    remoteFileMetadataRepository = mockk()
    createCloudFileUseCase = CreateCloudFileUseCase(
      getLocalUuIdUseCase = getLocalUuIdUseCase,
      remoteFileMetadataRepository = remoteFileMetadataRepository
    )
  }

  @Test
  fun invoke_withReminder_shouldCreateCloudFileWithCorrectNameAndExtension() = runBlocking {
    // Arrange - Create a reminder with realistic data
    val reminderUuId = "reminder-uuid-12345"
    val reminder = Reminder(
      summary = "Doctor appointment",
      uuId = reminderUuId,
      groupUuId = "default-group",
      eventTime = "2025-10-30 14:00",
      reminderType = 0
    )
    val existingMetadata = RemoteFileMetadata(
      id = "cloud-id-123",
      name = "$reminderUuId.ta2",
      lastModified = 123456789L,
      size = 1024,
      source = "GDRIVE",
      localUuId = reminderUuId,
      fileExtension = ".ta2",
      version = 1L,
      rev = "rev1"
    )

    every { getLocalUuIdUseCase(reminder) } returns reminderUuId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(reminderUuId) } returns existingMetadata

    // Act
    val result = createCloudFileUseCase(DataType.Reminders, reminder)

    // Assert
    assertNotNull(result)
    assertEquals("cloud-id-123", result.id)
    assertEquals("$reminderUuId.ta2", result.name)
    assertEquals(".ta2", result.fileExtension)
    coVerify(exactly = 1) { remoteFileMetadataRepository.getByLocalUuId(reminderUuId) }
  }

  @Test
  fun invoke_withBirthday_shouldCreateCloudFileWithBirthdayUuId() = runBlocking {
    // Arrange - Birthday with all required fields
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
    val existingMetadata = RemoteFileMetadata(
      id = "birthday-cloud-id",
      name = "$birthdayUuId.gr2",
      lastModified = 987654321L,
      size = 512,
      source = "DROPBOX",
      localUuId = birthdayUuId,
      fileExtension = ".gr2",
      version = 2L,
      rev = "rev2"
    )

    every { getLocalUuIdUseCase(birthday) } returns birthdayUuId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(birthdayUuId) } returns existingMetadata

    // Act
    val result = createCloudFileUseCase(DataType.Birthdays, birthday)

    // Assert
    assertEquals("birthday-cloud-id", result.id)
    assertEquals("$birthdayUuId.gr2", result.name)
    assertEquals(".gr2", result.fileExtension)
  }

  @Test
  fun invoke_withOldNote_shouldCreateCloudFileWithNoteKey() = runBlocking {
    // Arrange - Old note with key field
    val noteKey = "note-key-abc123"
    val note = OldNote(
      summary = "Meeting notes",
      key = noteKey,
      date = "2025-10-29",
      color = 0xFF0000,
      uniqueId = 12345
    )

    every { getLocalUuIdUseCase(note) } returns noteKey
    coEvery { remoteFileMetadataRepository.getByLocalUuId(noteKey) } returns null

    // Act
    val result = createCloudFileUseCase(DataType.Notes, note)

    // Assert - When no existing metadata, id should be empty
    assertEquals("", result.id)
    assertEquals("$noteKey.no2", result.name)
    assertEquals(".no2", result.fileExtension)
    assertEquals(0, result.size)
    assertEquals(0L, result.lastModified)
  }

  @Test
  fun invoke_withReminderGroup_shouldCreateCloudFileWithGroupUuId() = runBlocking {
    // Arrange - Reminder group
    val groupUuId = "group-uuid-xyz789"
    val reminderGroup = ReminderGroup(
      groupTitle = "Work Reminders",
      groupUuId = groupUuId,
      groupColor = 0x00FF00,
      groupDateTime = "2025-10-29 10:00",
      isDefaultGroup = false,
      syncState = SyncState.Synced
    )

    every { getLocalUuIdUseCase(reminderGroup) } returns groupUuId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(groupUuId) } returns null

    // Act
    val result = createCloudFileUseCase(DataType.Groups, reminderGroup)

    // Assert
    assertEquals("$groupUuId.bi2", result.name)
    assertEquals(".bi2", result.fileExtension)
    assertTrue(result.id.isEmpty())
  }

  @Test
  fun invoke_withPlace_shouldCreateCloudFileWithPlaceId() = runBlocking {
    // Arrange - Place with location data
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

    every { getLocalUuIdUseCase(place) } returns placeId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(placeId) } returns null

    // Act
    val result = createCloudFileUseCase(DataType.Places, place)

    // Assert
    assertEquals("$placeId.pl2", result.name)
    assertEquals(".pl2", result.fileExtension)
  }

  @Test
  fun invoke_withSettingsModel_shouldCreateCloudFileWithAppPrefix() = runBlocking {
    // Arrange - Settings model with configuration data
    val settingsModel = SettingsModel(
      mapOf(
        "theme" to "dark",
        "language" to "en",
        "notifications" to true
      )
    )
    val settingsUuId = "settings-uuid"

    every { getLocalUuIdUseCase(settingsModel) } returns settingsUuId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(settingsUuId) } returns null

    // Act
    val result = createCloudFileUseCase(DataType.Settings, settingsModel)

    // Assert - Settings use "app" prefix instead of uuid
    assertEquals("app.settings", result.name)
    assertEquals(".settings", result.fileExtension)
  }

  @Test
  fun invoke_withExistingMetadata_shouldReuseCloudFileId() = runBlocking {
    // Arrange - Test that existing cloud file ID is preserved
    val reminderUuId = "existing-reminder-uuid"
    val reminder = Reminder(
      summary = "Existing reminder",
      uuId = reminderUuId,
      groupUuId = "group1",
      eventTime = "2025-11-01 09:00",
      reminderType = 1
    )
    val existingMetadata = RemoteFileMetadata(
      id = "existing-cloud-id-999",
      name = "$reminderUuId.ta2",
      lastModified = 555555555L,
      size = 2048,
      source = "GDRIVE",
      localUuId = reminderUuId,
      fileExtension = ".ta2",
      version = 5L,
      rev = "rev5"
    )

    every { getLocalUuIdUseCase(reminder) } returns reminderUuId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(reminderUuId) } returns existingMetadata

    // Act
    val result = createCloudFileUseCase(DataType.Reminders, reminder)

    // Assert - Should reuse the existing cloud ID
    assertEquals("existing-cloud-id-999", result.id)
    coVerify(exactly = 1) { remoteFileMetadataRepository.getByLocalUuId(reminderUuId) }
  }

  @Test
  fun invoke_withUnsupportedDataType_shouldThrowException() {
    // Arrange - Use an unsupported object type
    val unsupportedObject = "This is a string, not a supported type"
    val localUuId = "some-uuid"

    every { getLocalUuIdUseCase(unsupportedObject) } returns localUuId
    coEvery { remoteFileMetadataRepository.getByLocalUuId(localUuId) } returns null

    // Act & Assert - Should throw IllegalArgumentException
    var exceptionThrown = false
    try {
      runBlocking {
        createCloudFileUseCase(DataType.Reminders, unsupportedObject)
      }
    } catch (e: IllegalArgumentException) {
      exceptionThrown = true
      assertTrue(e.message?.contains("Unsupported data type") == true)
    }
    assertTrue("Expected IllegalArgumentException to be thrown", exceptionThrown)
  }

  @Test
  fun invoke_withMultipleDifferentTypes_shouldGenerateCorrectExtensions() = runBlocking {
    // Arrange - Test that different data types get correct extensions
    val reminder = Reminder(summary = "Test", uuId = "r1")
    val birthday = Birthday(name = "Test", uuId = "b1", syncState = SyncState.Synced)
    val note = OldNote(summary = "Test", key = "n1")

    every { getLocalUuIdUseCase(any()) } returnsMany listOf("r1", "b1", "n1")
    coEvery { remoteFileMetadataRepository.getByLocalUuId(any()) } returns null

    // Act
    val reminderFile = createCloudFileUseCase(DataType.Reminders, reminder)
    val birthdayFile = createCloudFileUseCase(DataType.Birthdays, birthday)
    val noteFile = createCloudFileUseCase(DataType.Notes, note)

    // Assert - Each type should have correct extension
    assertEquals(".ta2", reminderFile.fileExtension)
    assertTrue(reminderFile.name.endsWith(".ta2"))

    assertEquals(".gr2", birthdayFile.fileExtension)
    assertTrue(birthdayFile.name.endsWith(".gr2"))

    assertEquals(".no2", noteFile.fileExtension)
    assertTrue(noteFile.name.endsWith(".no2"))
  }
}
