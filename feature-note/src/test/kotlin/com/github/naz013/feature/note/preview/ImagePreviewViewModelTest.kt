package com.github.naz013.feature.note.preview

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelStore
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ImagePreviewViewModelTest : BaseTest() {
  private val imagesSingleton = mockk<ImagesSingleton>(relaxed = true)

  private lateinit var viewModel: ImagePreviewViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // ImagePreviewViewModel.state runs loadInternal() in onStart on every collection - every test
    // collects state at least once, so a default stub avoids an unstubbed-call failure.
    every { imagesSingleton.getCurrent() } returns emptyList()
    every { imagesSingleton.getColor() } returns Color.Unspecified

    viewModel =
      ImagePreviewViewModel(
        initialPosition = 1,
        imagesSingleton = imagesSingleton,
        dispatcherProvider = mockDispatcherProvider(),
      )
  }

  private fun image(id: Int) = UiNoteImage(id = id, fileName = "image_$id.jpg")

  @Test
  fun `loads images, initial position and background into state on first collection`() =
    runTest {
      val images = listOf(image(1), image(2))
      every { imagesSingleton.getCurrent() } returns images
      every { imagesSingleton.getColor() } returns Color.Red

      val state = viewModel.state.first()

      assertEquals(images, state.images)
      assertEquals(1, state.position)
      assertEquals(Color.Red, state.background)
    }

  @Test
  fun `each fresh collection reloads images from the singleton`() =
    runTest {
      every { imagesSingleton.getCurrent() } returns listOf(image(1))
      assertEquals(listOf(image(1)), viewModel.state.first().images)

      every { imagesSingleton.getCurrent() } returns listOf(image(1), image(2))
      assertEquals(listOf(image(1), image(2)), viewModel.state.first().images)
    }

  @Test
  fun `onPageChanged updates the position while the state flow is being collected`() {
    // Use a real (non-test) Unconfined scope to keep a single collection of `state` alive so the
    // onStart{loadInternal()} reload runs only once here - loadInternal() fully replaces the state
    // object (including position), so re-collecting via `.first()` after onPageChanged would wipe
    // the update instead of demonstrating it.
    val collected = mutableListOf<ImagePreviewState>()
    val collectorScope = CoroutineScope(Dispatchers.Unconfined)
    val job = collectorScope.launch { viewModel.state.toList(collected) }

    viewModel.onPageChanged(2)

    assertEquals(2, collected.last().position)

    job.cancel()
    collectorScope.cancel()
  }

  @Test
  fun `onCleared clears the images singleton`() {
    val store = ViewModelStore()
    store.put("imagePreview", viewModel)

    store.clear()

    verify(exactly = 1) { imagesSingleton.clear() }
  }
}
