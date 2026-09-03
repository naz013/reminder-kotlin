package com.github.naz013.logic.note

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.demophoto.DemoPhoto
import com.github.naz013.demophoto.DemoPhotoDownloader
import com.github.naz013.domain.note.NoteWithImages
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InsertDemoNotesUseCaseTest {
  private val saveNoteUseCase = mockk<SaveNoteUseCase>(relaxed = true)
  private val noteImageRepository = mockk<NoteImageRepository>()
  private val demoPhotoDownloader = mockk<DemoPhotoDownloader>()
  private val dateTimeManager = mockk<DateTimeManager>()

  private lateinit var useCase: InsertDemoNotesUseCase

  @Before
  fun setUp() {
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-01-01 00:00:00"
    every { noteImageRepository.saveTemporaryImage(any(), any()) } returns "/tmp/demo_note_photo.jpg"

    useCase =
      InsertDemoNotesUseCase(
        saveNoteUseCase = saveNoteUseCase,
        noteImageRepository = noteImageRepository,
        demoPhotoDownloader = demoPhotoDownloader,
        dateTimeManager = dateTimeManager,
      )
  }

  @Test
  fun `invoke saves three notes`() =
    runTest {
      coEvery { demoPhotoDownloader.downloadRandomWallpaper() } returns
        DemoPhoto(byteArrayOf(1, 2, 3), "Jane Doe", "https://unsplash.com/photos/42")

      useCase()

      coVerify(exactly = 3) { saveNoteUseCase(any()) }
    }

  @Test
  fun `invoke attaches exactly one image and credits the photographer when a photo downloads`() =
    runTest {
      coEvery { demoPhotoDownloader.downloadRandomWallpaper() } returns
        DemoPhoto(byteArrayOf(1, 2, 3), "Jane Doe", "https://unsplash.com/photos/42")
      val captured = mutableListOf<NoteWithImages>()
      coEvery { saveNoteUseCase(capture(captured)) } returns Unit

      useCase()

      val withImages = captured.filter { it.images.isNotEmpty() }
      assertEquals(1, withImages.size)
      assertEquals(1, withImages.first().images.size)
      assertTrue(withImages.first().note?.content?.text?.contains("Jane Doe") == true)
    }

  @Test
  fun `invoke still saves three notes when the photo download fails`() =
    runTest {
      coEvery { demoPhotoDownloader.downloadRandomWallpaper() } returns null
      val captured = mutableListOf<NoteWithImages>()
      coEvery { saveNoteUseCase(capture(captured)) } returns Unit

      useCase()

      coVerify(exactly = 3) { saveNoteUseCase(any()) }
      assertTrue(captured.none { it.images.isNotEmpty() })
    }
}
