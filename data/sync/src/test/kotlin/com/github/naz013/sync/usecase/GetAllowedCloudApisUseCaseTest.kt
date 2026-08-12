package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.cache.SyncApiSessionCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetAllowedCloudApisUseCase].
 *
 * Tests the retrieval and caching mechanism of allowed cloud APIs,
 * covering scenarios with cache hits, cache misses, empty results,
 * and multiple cloud providers.
 */
class GetAllowedCloudApisUseCaseTest {

  private lateinit var cloudApiProvider: CloudApiProvider
  private lateinit var syncApiSessionCache: SyncApiSessionCache
  private lateinit var getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase

  @Before
  fun setUp() {
    cloudApiProvider = mockk()
    syncApiSessionCache = mockk()
    getAllowedCloudApisUseCase = GetAllowedCloudApisUseCase(
      cloudApiProvider = cloudApiProvider,
      syncApiSessionCache = syncApiSessionCache
    )
  }

  @Test
  fun `invoke when cache contains apis should return cached apis without fetching`() {
    // Arrange - Cache hit scenario with multiple cloud providers
    val cachedApi1 = mockk<CloudFileApi>(relaxed = true)
    val cachedApi2 = mockk<CloudFileApi>(relaxed = true)
    val cachedApis = listOf(cachedApi1, cachedApi2)
    every { syncApiSessionCache.getCached() } returns cachedApis

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should return cached APIs and not call provider
    assertEquals(cachedApis, result)
    assertEquals(2, result.size)
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    verify(exactly = 0) { cloudApiProvider.getAllowedCloudApis() }
    verify(exactly = 0) { syncApiSessionCache.cache(any()) }
  }

  @Test
  fun `invoke when cache is empty should fetch from provider and cache results`() {
    // Arrange - Cache miss scenario, need to fetch from provider
    val fetchedApi1 = mockk<CloudFileApi>(relaxed = true)
    val fetchedApi2 = mockk<CloudFileApi>(relaxed = true)
    val fetchedApis = listOf(fetchedApi1, fetchedApi2)
    every { syncApiSessionCache.getCached() } returns null
    every { cloudApiProvider.getAllowedCloudApis() } returns fetchedApis
    every { syncApiSessionCache.cache(fetchedApis) } returns Unit

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should fetch, cache, and return the APIs
    assertEquals(fetchedApis, result)
    assertEquals(2, result.size)
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    verify(exactly = 1) { cloudApiProvider.getAllowedCloudApis() }
    verify(exactly = 1) { syncApiSessionCache.cache(fetchedApis) }
  }

  @Test
  fun `invoke when cache is empty and provider returns empty list should cache empty list`() {
    // Arrange - No cloud APIs available from provider
    val emptyList = emptyList<CloudFileApi>()
    every { syncApiSessionCache.getCached() } returns null
    every { cloudApiProvider.getAllowedCloudApis() } returns emptyList
    every { syncApiSessionCache.cache(emptyList) } returns Unit

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should cache and return empty list
    assertEquals(emptyList, result)
    assertEquals(0, result.size)
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    verify(exactly = 1) { cloudApiProvider.getAllowedCloudApis() }
    verify(exactly = 1) { syncApiSessionCache.cache(emptyList) }
  }

  @Test
  fun `invoke when cache contains empty list should return empty list without fetching`() {
    // Arrange - Cache contains empty list (cached negative result)
    val emptyList = emptyList<CloudFileApi>()
    every { syncApiSessionCache.getCached() } returns emptyList

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should return cached empty list
    assertEquals(emptyList, result)
    assertEquals(0, result.size)
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    verify(exactly = 0) { cloudApiProvider.getAllowedCloudApis() }
    verify(exactly = 0) { syncApiSessionCache.cache(any()) }
  }

  @Test
  fun `invoke when cache contains single api should return single api`() {
    // Arrange - Cache contains single cloud provider (e.g., Google Drive)
    val singleApi = mockk<CloudFileApi>(relaxed = true)
    val cachedApis = listOf(singleApi)
    every { syncApiSessionCache.getCached() } returns cachedApis

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should return the single cached API
    assertEquals(cachedApis, result)
    assertEquals(1, result.size)
    assertEquals(singleApi, result[0])
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    verify(exactly = 0) { cloudApiProvider.getAllowedCloudApis() }
  }

  @Test
  fun `invoke multiple times with cache should return same cached instance`() {
    // Arrange - Test that cache is used consistently across multiple invocations
    val cachedApi = mockk<CloudFileApi>(relaxed = true)
    val cachedApis = listOf(cachedApi)
    every { syncApiSessionCache.getCached() } returns cachedApis

    // Act - Call multiple times
    val result1 = getAllowedCloudApisUseCase()
    val result2 = getAllowedCloudApisUseCase()
    val result3 = getAllowedCloudApisUseCase()

    // Assert - All results should be identical and from cache
    assertEquals(cachedApis, result1)
    assertEquals(cachedApis, result2)
    assertEquals(cachedApis, result3)
    assertEquals(result1, result2)
    assertEquals(result2, result3)
    verify(exactly = 3) { syncApiSessionCache.getCached() }
    verify(exactly = 0) { cloudApiProvider.getAllowedCloudApis() }
  }

  @Test
  fun `invoke when provider returns large list of apis should cache and return all`() {
    // Arrange - Realistic scenario with multiple cloud providers configured
    val apis = (1..5).map { mockk<CloudFileApi>(relaxed = true) }
    every { syncApiSessionCache.getCached() } returns null
    every { cloudApiProvider.getAllowedCloudApis() } returns apis
    every { syncApiSessionCache.cache(apis) } returns Unit

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should handle and cache multiple APIs correctly
    assertEquals(apis, result)
    assertEquals(5, result.size)
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    verify(exactly = 1) { cloudApiProvider.getAllowedCloudApis() }
    verify(exactly = 1) { syncApiSessionCache.cache(apis) }
  }

  @Test
  fun `invoke caching behavior should prioritize cache over provider`() {
    // Arrange - Verify cache is always checked first
    val cachedApis = listOf(mockk<CloudFileApi>(relaxed = true))
    val providerApis = listOf(mockk<CloudFileApi>(relaxed = true))
    every { syncApiSessionCache.getCached() } returns cachedApis
    every { cloudApiProvider.getAllowedCloudApis() } returns providerApis

    // Act
    val result = getAllowedCloudApisUseCase()

    // Assert - Should use cached value, not provider value
    assertEquals(cachedApis, result)
    assertEquals(1, result.size)
    verify(exactly = 1) { syncApiSessionCache.getCached() }
    // Provider should never be called when cache has data
    verify(exactly = 0) { cloudApiProvider.getAllowedCloudApis() }
    verify(exactly = 0) { syncApiSessionCache.cache(any()) }
  }
}

