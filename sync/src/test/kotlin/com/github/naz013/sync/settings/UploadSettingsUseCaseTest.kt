package com.github.naz013.sync.settings

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncSettings
import com.github.naz013.sync.usecase.CreateCloudFileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests for [UploadSettingsUseCase].
 *
 * Tests the functionality of uploading settings data to cloud storage providers,
 * including successful uploads, error handling, and edge cases.
 */
class UploadSettingsUseCaseTest {

  private lateinit var createCloudFileUseCase: CreateCloudFileUseCase
  private lateinit var syncSettings: SyncSettings
  private lateinit var cloudApiProvider: CloudApiProvider
  private lateinit var syncDataConverter: SyncDataConverter
  private lateinit var uploadSettingsUseCase: UploadSettingsUseCase

  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    createCloudFileUseCase = mockk()
    syncSettings = mockk()
    cloudApiProvider = mockk()
    syncDataConverter = mockk()
    mockCloudFileApi = mockk()

    uploadSettingsUseCase = UploadSettingsUseCase(
      createCloudFileUseCase = createCloudFileUseCase,
      syncSettings = syncSettings,
      cloudApiProvider = cloudApiProvider,
      syncDataConverter = syncDataConverter
    )
  }

  @Test
  fun invoke_withValidSettings_shouldUploadToAllCloudApis() = runBlocking {
    // Arrange
    val settingsModel = SettingsModel(mapOf("key1" to "value1", "key2" to 42))
    val cloudFile = CloudFile(id = "test-id", name = "app.settings", fileExtension = ".settings")
    val inputStream = ByteArrayInputStream("test data".toByteArray())
    val cloudApis = listOf(mockCloudFileApi)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } returns inputStream
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile

    // Act
    uploadSettingsUseCase()

    // Assert
    verify(exactly = 1) { syncSettings.getSettings() }
    coVerify(exactly = 1) { createCloudFileUseCase(DataType.Settings, settingsModel) }
    verify(exactly = 1) { cloudApiProvider.getAllowedCloudApis() }
    coVerify(exactly = 1) { syncDataConverter.create(settingsModel) }
    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(inputStream, cloudFile) }
  }

  @Test
  fun invoke_withMultipleCloudApis_shouldUploadToAllApis() = runBlocking {
    // Arrange
    val settingsModel = SettingsModel(mapOf("theme" to "dark", "language" to "en"))
    val cloudFile = CloudFile(id = "test-id", name = "app.settings", fileExtension = ".settings")
    val inputStream1 = ByteArrayInputStream("data1".toByteArray())
    val inputStream2 = ByteArrayInputStream("data2".toByteArray())
    val mockCloudFileApi2 = mockk<CloudFileApi>()
    val cloudApis = listOf(mockCloudFileApi, mockCloudFileApi2)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } returnsMany listOf(inputStream1, inputStream2)
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile
    coEvery { mockCloudFileApi2.uploadFile(any(), any()) } returns cloudFile

    // Act
    uploadSettingsUseCase()

    // Assert
    coVerify(exactly = 2) { syncDataConverter.create(settingsModel) }
    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(inputStream1, cloudFile) }
    coVerify(exactly = 1) { mockCloudFileApi2.uploadFile(inputStream2, cloudFile) }
  }

  @Test
  fun invoke_withEmptyCloudApisList_shouldNotUploadAnything() = runBlocking {
    // Arrange
    val settingsModel = SettingsModel(mapOf("setting1" to true))
    val cloudFile = CloudFile(id = "test-id", name = "app.settings", fileExtension = ".settings")

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns emptyList()

    // Act
    uploadSettingsUseCase()

    // Assert
    verify(exactly = 1) { syncSettings.getSettings() }
    coVerify(exactly = 1) { createCloudFileUseCase(DataType.Settings, settingsModel) }
    // Should not call syncDataConverter or upload when no cloud APIs available
    coVerify(exactly = 0) { syncDataConverter.create(any()) }
  }

  @Test
  fun invoke_withEmptySettingsData_shouldStillUpload() = runBlocking {
    // Arrange - Settings with empty data map
    val settingsModel = SettingsModel(emptyMap<String, Any>())
    val cloudFile = CloudFile(id = "empty-id", name = "app.settings", fileExtension = ".settings")
    val inputStream = ByteArrayInputStream(byteArrayOf())
    val cloudApis = listOf(mockCloudFileApi)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } returns inputStream
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile

    // Act
    uploadSettingsUseCase()

    // Assert
    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(inputStream, cloudFile) }
  }

  @Test
  fun invoke_withComplexSettingsData_shouldUploadCorrectly() = runBlocking {
    // Arrange - Settings with nested data structures
    val complexData = mapOf(
      "string" to "value",
      "number" to 123,
      "boolean" to true,
      "list" to listOf(1, 2, 3),
      "map" to mapOf("nested" to "data")
    )
    val settingsModel = SettingsModel(complexData)
    val cloudFile = CloudFile(id = "complex-id", name = "app.settings", fileExtension = ".settings")
    val inputStream = ByteArrayInputStream("complex data".toByteArray())
    val cloudApis = listOf(mockCloudFileApi)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } returns inputStream
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile

    // Act
    uploadSettingsUseCase()

    // Assert
    verify(exactly = 1) { syncSettings.getSettings() }
    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(inputStream, cloudFile) }
  }

  @Test(expected = Exception::class)
  fun invoke_whenSyncSettingsFails_shouldPropagateException() = runBlocking {
    // Arrange
    every { syncSettings.getSettings() } throws IllegalStateException("Settings unavailable")

    // Act
    uploadSettingsUseCase()

    // Assert - Exception should be thrown
  }

  @Test(expected = Exception::class)
  fun invoke_whenCreateCloudFileFails_shouldPropagateException() = runBlocking {
    // Arrange
    val settingsModel = SettingsModel(mapOf("key" to "value"))

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } throws
      IllegalArgumentException("Invalid data type")

    // Act
    uploadSettingsUseCase()

    // Assert - Exception should be thrown
  }

  @Test(expected = Exception::class)
  fun invoke_whenDataConverterFails_shouldPropagateException() = runBlocking {
    // Arrange
    val settingsModel = SettingsModel(mapOf("key" to "value"))
    val cloudFile = CloudFile(id = "test-id", name = "app.settings", fileExtension = ".settings")
    val cloudApis = listOf(mockCloudFileApi)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } throws
      RuntimeException("Failed to serialize data")

    // Act
    uploadSettingsUseCase()

    // Assert - Exception should be thrown
  }

  @Test(expected = Exception::class)
  fun invoke_whenUploadFails_shouldPropagateException() = runBlocking {
    // Arrange
    val settingsModel = SettingsModel(mapOf("key" to "value"))
    val cloudFile = CloudFile(id = "test-id", name = "app.settings", fileExtension = ".settings")
    val inputStream = ByteArrayInputStream("test".toByteArray())
    val cloudApis = listOf(mockCloudFileApi)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } returns inputStream
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } throws
      RuntimeException("Network error during upload")

    // Act
    uploadSettingsUseCase()

    // Assert - Exception should be thrown
  }

  @Test
  fun invoke_withThreeCloudApis_shouldCreateThreeSeparateStreams() = runBlocking {
    // Arrange - Test that a new stream is created for each cloud API
    val settingsModel = SettingsModel(mapOf("key" to "value"))
    val cloudFile = CloudFile(id = "test-id", name = "app.settings", fileExtension = ".settings")
    val stream1 = ByteArrayInputStream("data1".toByteArray())
    val stream2 = ByteArrayInputStream("data2".toByteArray())
    val stream3 = ByteArrayInputStream("data3".toByteArray())
    val api1 = mockk<CloudFileApi>()
    val api2 = mockk<CloudFileApi>()
    val api3 = mockk<CloudFileApi>()
    val cloudApis = listOf(api1, api2, api3)

    every { syncSettings.getSettings() } returns settingsModel
    coEvery { createCloudFileUseCase(DataType.Settings, settingsModel) } returns cloudFile
    every { cloudApiProvider.getAllowedCloudApis() } returns cloudApis
    coEvery { syncDataConverter.create(settingsModel) } returnsMany listOf(stream1, stream2, stream3)
    coEvery { api1.uploadFile(any(), any()) } returns cloudFile
    coEvery { api2.uploadFile(any(), any()) } returns cloudFile
    coEvery { api3.uploadFile(any(), any()) } returns cloudFile

    // Act
    uploadSettingsUseCase()

    // Assert - Should create 3 separate streams
    coVerify(exactly = 3) { syncDataConverter.create(settingsModel) }
    coVerify(exactly = 1) { api1.uploadFile(stream1, cloudFile) }
    coVerify(exactly = 1) { api2.uploadFile(stream2, cloudFile) }
    coVerify(exactly = 1) { api3.uploadFile(stream3, cloudFile) }
  }
}
