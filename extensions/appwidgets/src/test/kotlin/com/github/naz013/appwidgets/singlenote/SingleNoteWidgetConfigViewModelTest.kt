package com.github.naz013.appwidgets.singlenote

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.singlenote.adapter.RecyclableUiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.NoteRepository
import com.github.naz013.ui.note.UiNoteListItemAdapter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SingleNoteWidgetConfigViewModelTest {

  private val dispatcher = UnconfinedTestDispatcher()
  private val dispatcherProvider = mockk<DispatcherProvider>()
  private val prefsProvider = mockk<SingleNoteWidgetPrefsProvider>(relaxed = true)
  private val noteRepository = mockk<NoteRepository>()
  private val appWidgetPreferences = mockk<AppWidgetPreferences>(relaxed = true)
  private val noteWidgetPreferences = mockk<NoteWidgetPreferences>()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    every { dispatcherProvider.io() } returns dispatcher
    every { dispatcherProvider.default() } returns dispatcher
    every { prefsProvider.getNoteId() } returns null
    every { noteWidgetPreferences.getNoteColors() } returns emptyList()
    coEvery { noteRepository.getAll(isArchived = false) } returns emptyList()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel() = SingleNoteWidgetConfigViewModel(
    appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true),
    dispatcherProvider = dispatcherProvider,
    prefsProvider = prefsProvider,
    analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true),
    noteRepository = noteRepository,
    uiNoteListItemAdapter = mockk<UiNoteListItemAdapter>(relaxed = true),
    previewAdapter = mockk<RecyclableUiNoteWidgetAdapter>(relaxed = true),
    uiNoteWidgetAdapter = mockk<UiNoteWidgetAdapter>(relaxed = true),
    appWidgetPreferences = appWidgetPreferences,
    noteWidgetPreferences = noteWidgetPreferences
  )

  @Test
  fun `preview is not loading before a note is selected`() {
    val viewModel = createViewModel()

    assertFalse(viewModel.state.value.isPreviewLoading)
  }

  @Test
  fun `shows loading while the selected note is being fetched`() {
    val gate = CompletableDeferred<Nothing?>()
    coEvery { noteRepository.getById("1") } coAnswers { gate.await() }
    val viewModel = createViewModel()

    viewModel.onNoteSelected("1")

    assertTrue(viewModel.state.value.isPreviewLoading)
  }

  @Test
  fun `stops loading when the selected note no longer exists`() {
    coEvery { noteRepository.getById("1") } returns null
    val viewModel = createViewModel()

    viewModel.onNoteSelected("1")

    assertFalse(viewModel.state.value.isPreviewLoading)
  }
}
