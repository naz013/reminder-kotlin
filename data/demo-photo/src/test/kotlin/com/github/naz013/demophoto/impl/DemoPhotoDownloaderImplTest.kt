package com.github.naz013.demophoto.impl

import com.github.naz013.demophoto.DemoPhoto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DemoPhotoDownloaderImplTest {
  private val service = mockk<PicsumService>()
  private val cache = mockk<DemoPhotoCache>(relaxed = true)

  private lateinit var downloader: DemoPhotoDownloaderImpl

  @Before
  fun setUp() {
    downloader = DemoPhotoDownloaderImpl(service, cache)
  }

  @Test
  fun `returns the cached photo without calling the service`() =
    runTest {
      val cached = DemoPhoto(byteArrayOf(9), "Cached Photographer", "https://unsplash.com/photos/cached")
      every { cache.read() } returns cached

      val result = downloader.downloadRandomWallpaper()

      assertEquals(cached, result)
      coVerify(exactly = 0) { service.list(any(), any()) }
    }

  @Test
  fun `downloads, caches and returns a random photo when nothing is cached`() =
    runTest {
      every { cache.read() } returns null
      val dto = PicsumPhotoDto(id = "42", author = "Jane Doe", url = "https://unsplash.com/photos/42")
      coEvery { service.list(any(), any()) } returns listOf(dto)
      val responseBody = mockk<ResponseBody> { every { bytes() } returns byteArrayOf(1, 2, 3) }
      coEvery { service.downloadPhoto("42", any(), any()) } returns responseBody

      val result = downloader.downloadRandomWallpaper()

      assertEquals("Jane Doe", result?.photographerName)
      assertEquals("https://unsplash.com/photos/42", result?.sourcePageUrl)
      assertEquals(listOf<Byte>(1, 2, 3), result?.bytes?.toList())
      coVerify(exactly = 1) { cache.write(any()) }
    }

  @Test
  fun `returns null when the download fails`() =
    runTest {
      every { cache.read() } returns null
      coEvery { service.list(any(), any()) } throws RuntimeException("offline")

      val result = downloader.downloadRandomWallpaper()

      assertNull(result)
    }
}
