package com.github.naz013.feature.note.create

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.request.ImageResult
import com.github.naz013.feature.note.R
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.UiNoteImageState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers [NoteEditViewModel]'s image pipeline: adding a bitmap (e.g. from the camera), decoding
 * picked/dropped uris via [com.github.naz013.feature.note.create.images.ImageDecoder], downloading an
 * image from a pasted url, removing an image, and opening the full-screen image preview.
 */
class NoteEditViewModelImagesTest : NoteEditViewModelTestSupport() {

  @Test
  fun `addBitmap appends a loading placeholder then replaces it with the saved image`() {
    val viewModel = buildViewModel()
    every { noteImageRepository.saveTemporaryImage(any(), any()) } returns "/tmp/new-image.png"
    val bitmap = mockk<Bitmap>(relaxed = true)

    viewModel.addBitmap(bitmap)

    val images = viewModel.state.value.images
    assertEquals(1, images.size)
    assertEquals(UiNoteImageState.READY, images[0].state)
    assertEquals("/tmp/new-image.png", images[0].filePath)
  }

  @Test
  fun `addBitmap appends after any existing images instead of replacing them`() {
    stubImageDecoder { UiNoteImage(id = 1, fileName = "first.jpg", state = UiNoteImageState.READY) }
    val viewModel = buildViewModel()
    viewModel.addMultiple(listOf(mockk<Uri>(relaxed = true)))
    every { noteImageRepository.saveTemporaryImage(any(), any()) } returns "/tmp/second.png"

    viewModel.addBitmap(mockk<Bitmap>(relaxed = true))

    val images = viewModel.state.value.images
    assertEquals(2, images.size)
    assertEquals("first.jpg", images[0].fileName)
    assertEquals("/tmp/second.png", images[1].filePath)
  }

  @Test
  fun `addMultiple decodes each uri and appends the ready images in order`() {
    val readyImages = mutableMapOf<Uri, UiNoteImage>()
    stubImageDecoder { uri -> readyImages.getValue(uri) }
    val uriA = mockk<Uri>(relaxed = true)
    val uriB = mockk<Uri>(relaxed = true)
    readyImages[uriA] = UiNoteImage(id = 1, fileName = "a.jpg", state = UiNoteImageState.READY)
    readyImages[uriB] = UiNoteImage(id = 2, fileName = "b.jpg", state = UiNoteImageState.READY)
    val viewModel = buildViewModel()

    viewModel.addMultiple(listOf(uriA, uriB))

    val images = viewModel.state.value.images
    assertEquals(listOf("a.jpg", "b.jpg"), images.map { it.fileName })
  }

  @Test
  fun `addMultiple drops an image that failed to decode`() {
    stubImageDecoder { UiNoteImage(id = 0, fileName = "broken.jpg", state = UiNoteImageState.ERROR) }
    val viewModel = buildViewModel()

    viewModel.addMultiple(listOf(mockk<Uri>(relaxed = true)))

    assertEquals(emptyList<UiNoteImage>(), viewModel.state.value.images)
  }

  @Test
  fun `removeImage removes the image at the given position`() {
    stubImageDecoder { UiNoteImage(id = 1, fileName = "keep-me.jpg", state = UiNoteImageState.READY) }
    val viewModel = buildViewModel()
    viewModel.addMultiple(listOf(mockk<Uri>(relaxed = true)))
    every { noteImageRepository.saveTemporaryImage(any(), any()) } returns "/tmp/second.png"
    viewModel.addBitmap(mockk<Bitmap>(relaxed = true))

    viewModel.removeImage(0)

    val images = viewModel.state.value.images
    assertEquals(1, images.size)
    assertEquals("/tmp/second.png", images[0].filePath)
  }

  @Test
  fun `removeImage does nothing when the position is out of bounds`() {
    stubImageDecoder { UiNoteImage(id = 1, fileName = "only.jpg", state = UiNoteImageState.READY) }
    val viewModel = buildViewModel()
    viewModel.addMultiple(listOf(mockk<Uri>(relaxed = true)))

    viewModel.removeImage(5)

    assertEquals(1, viewModel.state.value.images.size)
  }

  @Test
  fun `downloadImageFromUrl shows an error immediately for a malformed url`() {
    every { textProvider.getText(R.string.wrong_url) } returns "Wrong URL"
    val viewModel = buildViewModel()

    viewModel.downloadImageFromUrl("not a url")

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Wrong URL"), event)
  }

  @Test
  fun `downloadImageFromUrl adds the downloaded bitmap for a valid url`() {
    val bitmap = mockk<Bitmap>(relaxed = true)
    val drawable = mockk<BitmapDrawable>(relaxed = true)
    every { drawable.bitmap } returns bitmap
    val imageResult = mockk<ImageResult>()
    every { imageResult.drawable } returns drawable
    coEvery { imageLoader.execute(any()) } returns imageResult
    every { noteImageRepository.saveTemporaryImage(any(), any()) } returns "/tmp/downloaded.png"
    val viewModel = buildViewModel()

    viewModel.downloadImageFromUrl("http://example.com/image.png")

    val images = viewModel.state.value.images
    assertEquals(1, images.size)
    assertEquals("/tmp/downloaded.png", images[0].filePath)
  }

  @Test
  fun `downloadImageFromUrl shows a failure error when the download returns no drawable`() {
    every { textProvider.getText(R.string.failed_to_download) } returns "Download failed"
    val imageResult = mockk<ImageResult>()
    every { imageResult.drawable } returns null
    coEvery { imageLoader.execute(any()) } returns imageResult
    val viewModel = buildViewModel()

    viewModel.downloadImageFromUrl("http://example.com/image.png")

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Download failed"), event)
  }

  @Test
  fun `downloadImageFromUrl shows a failure error when the loader throws`() {
    every { textProvider.getText(R.string.failed_to_download) } returns "Download failed"
    coEvery { imageLoader.execute(any()) } throws RuntimeException("network error")
    val viewModel = buildViewModel()

    viewModel.downloadImageFromUrl("http://example.com/image.png")

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Download failed"), event)
  }

  @Test
  fun `onImageOpen stores the current images and background color then opens the preview`() {
    stubImageDecoder { UiNoteImage(id = 1, fileName = "a.jpg", state = UiNoteImageState.READY) }
    val viewModel = buildViewModel()
    viewModel.addMultiple(listOf(mockk<Uri>(relaxed = true)))

    viewModel.onImageOpen(0)

    val state = viewModel.state.value
    verify { imagesSingleton.setCurrent(images = state.images, backgroundColor = state.noteColors.background) }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.OpenImagePreview(0), event)
  }
}
