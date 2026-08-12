package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.sync.CloudApiProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [HasAnyCloudApiUseCase].
 *
 * Tests the detection of available cloud storage providers,
 * including scenarios with no APIs, single API, and multiple APIs.
 */
class HasAnyCloudApiUseCaseTest {

  private lateinit var getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
  private lateinit var hasAnyCloudApiUseCase: HasAnyCloudApiUseCase

  @Before
  fun setUp() {
    getAllowedCloudApisUseCase = mockk()
    hasAnyCloudApiUseCase = HasAnyCloudApiUseCase(
      getAllowedCloudApisUseCase = getAllowedCloudApisUseCase
    )
  }

  @Test
  fun `invoke when no cloud apis available should return false`() {
    // Arrange - No cloud storage providers configured
    every { getAllowedCloudApisUseCase.invoke() } returns emptyList()

    // Act
    val result = hasAnyCloudApiUseCase()

    // Assert
    assertFalse(result)
    verify(exactly = 1) { getAllowedCloudApisUseCase.invoke() }
  }

  @Test
  fun `invoke when one cloud api available should return true`() {
    // Arrange - Single cloud provider (Google Drive)
    val mockCloudApi = mockk<CloudFileApi>()
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudApi)

    // Act
    val result = hasAnyCloudApiUseCase()

    // Assert
    assertTrue(result)
    verify(exactly = 1) { getAllowedCloudApisUseCase.invoke() }
  }

  @Test
  fun `invoke when multiple cloud apis available should return true`() {
    // Arrange - Multiple cloud providers (Google Drive and Dropbox)
    val mockGoogleDrive = mockk<CloudFileApi>()
    val mockDropbox = mockk<CloudFileApi>()
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockGoogleDrive, mockDropbox)

    // Act
    val result = hasAnyCloudApiUseCase()

    // Assert
    assertTrue(result)
    verify(exactly = 1) { getAllowedCloudApisUseCase.invoke() }
  }

  @Test
  fun `invoke should delegate to cloud api provider`() {
    // Arrange - Verify the use case properly delegates to provider
    val mockCloudApi = mockk<CloudFileApi>()
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudApi)

    // Act
    hasAnyCloudApiUseCase()

    // Assert - Should call getAllowedCloudApis exactly once
    verify(exactly = 1) { getAllowedCloudApisUseCase.invoke() }
  }

  @Test
  fun `invoke multiple times should call provider each time`() {
    // Arrange - Test that each invocation queries the provider
    every { getAllowedCloudApisUseCase.invoke() } returns emptyList()

    // Act - Call multiple times
    hasAnyCloudApiUseCase()
    hasAnyCloudApiUseCase()
    hasAnyCloudApiUseCase()

    // Assert - Should call provider 3 times (no caching)
    verify(exactly = 3) { getAllowedCloudApisUseCase.invoke() }
  }

  @Test
  fun `invoke should return false for empty list regardless of initial state`() {
    // Arrange - Always empty list
    every { getAllowedCloudApisUseCase.invoke() } returns emptyList()

    // Act - Multiple calls
    val result1 = hasAnyCloudApiUseCase()
    val result2 = hasAnyCloudApiUseCase()
    val result3 = hasAnyCloudApiUseCase()

    // Assert - All calls should consistently return false
    assertFalse(result1)
    assertFalse(result2)
    assertFalse(result3)
  }

  @Test
  fun `invoke should return true when list contains single element`() {
    // Arrange - Exactly one API
    val mockApi = mockk<CloudFileApi>()
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockApi)

    // Act
    val result = hasAnyCloudApiUseCase()

    // Assert - One API is sufficient to return true
    assertTrue(result)
  }

  @Test
  fun `invoke should return true when list contains many elements`() {
    // Arrange - Large list of APIs (edge case: many providers)
    val mockApis = (1..10).map { mockk<CloudFileApi>() }
    every { getAllowedCloudApisUseCase.invoke() } returns mockApis

    // Act
    val result = hasAnyCloudApiUseCase()

    // Assert - Many APIs should still return true
    assertTrue(result)
  }

  @Test
  fun `invoke result should reflect current provider state`() {
    // Arrange - Provider state changes between calls
    every { getAllowedCloudApisUseCase.invoke() } returns emptyList() andThen listOf(mockk())

    // Act & Assert - First call: no APIs
    val result1 = hasAnyCloudApiUseCase()
    assertFalse(result1)

    // Act & Assert - Second call: one API available
    val result2 = hasAnyCloudApiUseCase()
    assertTrue(result2)
  }

  @Test
  fun `invoke should not throw exception when provider returns empty list`() {
    // Arrange - Empty list is valid scenario
    every { getAllowedCloudApisUseCase.invoke() } returns emptyList()

    // Act - Should not throw
    var exceptionThrown = false
    try {
      hasAnyCloudApiUseCase()
    } catch (e: Exception) {
      exceptionThrown = true
    }

    // Assert - No exception expected
    assertFalse("Should not throw exception for empty list", exceptionThrown)
  }
}

