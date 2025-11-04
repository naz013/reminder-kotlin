package com.github.naz013.cloudapi.dropbox

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileSearchParams
import com.github.naz013.cloudapi.Source
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests for [DropboxApiImpl].
 *
 * Tests the complete Dropbox integration including:
 * - Initialization and authorization
 * - Input validation
 * - Error handling and edge cases
 * - API lifecycle methods
 */
class DropboxApiImplTest {

  private lateinit var dropboxAuthManager: DropboxAuthManager
  private lateinit var dropboxApi: DropboxApiImpl

  @Before
  fun setUp() {
    dropboxAuthManager = mockk()
  }

  @Test
  fun `initialize with valid authorization should return true`() {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "valid-token-abc123"

    // Act
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Assert
    assertTrue("API should be initialized", dropboxApi.initialize())
    assertEquals("Source should be Dropbox", Source.Dropbox, dropboxApi.source)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `uploadFile with blank file name should throw IllegalArgumentException`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val inputStream = ByteArrayInputStream("test".toByteArray())
    val cloudFile = CloudFile(
      name = "  ", // Blank name
      fileExtension = ".ta2"
    )

    // Act & Assert - Exception expected
    dropboxApi.uploadFile(inputStream, cloudFile)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `uploadFile with blank file extension should throw IllegalArgumentException`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val inputStream = ByteArrayInputStream("test".toByteArray())
    val cloudFile = CloudFile(
      name = "test.txt",
      fileExtension = "  " // Blank extension
    )

    // Act & Assert - Exception expected
    dropboxApi.uploadFile(inputStream, cloudFile)
  }

  @Test(expected = IllegalStateException::class)
  fun `uploadFile when not initialized should throw IllegalStateException`() = runTest {
    // Arrange - Not initialized
    every { dropboxAuthManager.isAuthorized() } returns false
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val inputStream = ByteArrayInputStream("test".toByteArray())
    val cloudFile = CloudFile(
      name = "test.ta2",
      fileExtension = ".ta2"
    )

    // Act & Assert - Exception expected
    dropboxApi.uploadFile(inputStream, cloudFile)
  }

  @Test
  fun `findFile with blank name should return null`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val searchParams = CloudFileSearchParams(
      name = "  ",
      fileExtension = ".ta2"
    )

    // Act
    val result = dropboxApi.findFile(searchParams)

    // Assert
    assertNull("Result should be null for blank name", result)
  }

  @Test
  fun `findFile with blank extension should return null`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val searchParams = CloudFileSearchParams(
      name = "test.ta2",
      fileExtension = "  "
    )

    // Act
    val result = dropboxApi.findFile(searchParams)

    // Assert
    assertNull("Result should be null for blank extension", result)
  }

  @Test
  fun `findFile when not initialized should return null`() = runTest {
    // Arrange - Not initialized
    every { dropboxAuthManager.isAuthorized() } returns false
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val searchParams = CloudFileSearchParams(
      name = "test.ta2",
      fileExtension = ".ta2"
    )

    // Act
    val result = dropboxApi.findFile(searchParams)

    // Assert
    assertNull("Result should be null when not initialized", result)
  }

  @Test
  fun `findFiles with blank extension should return empty list`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act
    val result = dropboxApi.findFiles("  ")

    // Assert
    assertTrue("Result should be empty list", result.isEmpty())
  }

  @Test
  fun `findFiles when not initialized should return empty list`() = runTest {
    // Arrange - Not initialized
    every { dropboxAuthManager.isAuthorized() } returns false
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act
    val result = dropboxApi.findFiles(".ta2")

    // Assert
    assertTrue("Result should be empty list", result.isEmpty())
  }

  @Test
  fun `downloadFile with blank name should return null`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val cloudFile = CloudFile(
      name = "  ",
      fileExtension = ".ta2"
    )

    // Act
    val result = dropboxApi.downloadFile(cloudFile)

    // Assert
    assertNull("Result should be null for blank name", result)
  }

  @Test
  fun `downloadFile when not initialized should return null`() = runTest {
    // Arrange - Not initialized
    every { dropboxAuthManager.isAuthorized() } returns false
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    val cloudFile = CloudFile(
      name = "test.ta2",
      fileExtension = ".ta2"
    )

    // Act
    val result = dropboxApi.downloadFile(cloudFile)

    // Assert
    assertNull("Result should be null when not initialized", result)
  }

  @Test
  fun `deleteFile with blank name should return false`() = runTest {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act
    val result = dropboxApi.deleteFile("  ")

    // Assert
    assertFalse("Delete should fail for blank name", result)
  }

  @Test
  fun `deleteFile when not initialized should return false`() = runTest {
    // Arrange - Not initialized
    every { dropboxAuthManager.isAuthorized() } returns false
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act
    val result = dropboxApi.deleteFile("test.ta2")

    // Assert
    assertFalse("Delete should fail when not initialized", result)
  }

  @Test
  fun `initialize when not authorized should return false`() {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns false

    // Act
    dropboxApi = DropboxApiImpl(dropboxAuthManager)
    val result = dropboxApi.initialize()

    // Assert
    assertFalse("Initialize should fail when not authorized", result)
  }

  @Test
  fun `initialize with empty token should return false`() {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns ""

    // Act
    dropboxApi = DropboxApiImpl(dropboxAuthManager)
    val result = dropboxApi.initialize()

    // Assert
    assertFalse("Initialize should fail with empty token", result)
  }

  @Test
  fun `disconnect should reset initialized state`() {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act
    dropboxApi.disconnect()

    // Assert - Verify can't upload after disconnect
    runTest {
      try {
        val inputStream = ByteArrayInputStream("test".toByteArray())
        val cloudFile = CloudFile(name = "test.ta2", fileExtension = ".ta2")
        dropboxApi.uploadFile(inputStream, cloudFile)
        throw AssertionError("Should have thrown IllegalStateException")
      } catch (e: IllegalStateException) {
        assertEquals("DropboxApi is not initialized", e.message)
      }
    }
  }

  @Test
  fun `removeAllData when not initialized should return false`() = runTest {
    // Arrange - Not initialized
    every { dropboxAuthManager.isAuthorized() } returns false
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act
    val result = dropboxApi.removeAllData()

    // Assert
    assertFalse("Remove all data should fail when not initialized", result)
  }

  @Test
  fun `source property should return Dropbox`() {
    // Arrange
    every { dropboxAuthManager.isAuthorized() } returns true
    every { dropboxAuthManager.getOAuth2Token() } returns "token"
    dropboxApi = DropboxApiImpl(dropboxAuthManager)

    // Act & Assert
    assertEquals("Source should be Dropbox", Source.Dropbox, dropboxApi.source)
  }
}

