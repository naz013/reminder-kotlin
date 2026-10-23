package com.github.naz013.feature.places.list

import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import com.github.naz013.files.BackupTool
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.feature.places.usecase.DeletePlaceUseCase
import com.github.naz013.ui.map.MapStyle
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentFactory
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.viewmodel.UndoEvent
import com.github.naz013.repository.PlaceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import java.io.File

class PlacesViewModelTest : BaseTest() {
  private val backupTool = mockk<BackupTool>()
  private val placeRepository = mockk<PlaceRepository>()
  private val deletePlaceUseCase = mockk<DeletePlaceUseCase>(relaxed = true)
  private val mapStyle = mockk<MapStyle>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val intentFactory = mockk<IntentFactory>()

  private lateinit var viewModel: PlacesViewModel

  private fun place(
    id: String = "1",
    name: String = "Home",
  ) = Place(id = id, name = name, syncState = SyncState.Synced)

  @Before
  override fun setUp() {
    super.setUp()
    every { placeRepository.observeAll() } returns flowOf(emptyList())
    every { dateTimeManager.getPlaceDateTimeFromGmt(any()) } returns null

    viewModel = createViewModel()
  }

  private fun createViewModel() =
    PlacesViewModel(
      backupTool = backupTool,
      dispatcherProvider = mockDispatcherProvider(),
      placeRepository = placeRepository,
      deletePlaceUseCase = deletePlaceUseCase,
      mapStyle = mapStyle,
      dateTimeManager = dateTimeManager,
      textProvider = textProvider,
      intentFactory = intentFactory,
    )

  @Test
  fun `loads empty state when there are no places`() =
    runTest {
      val state = viewModel.screenState.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `loads places sorted by name on first collection`() =
    runTest {
      every { placeRepository.observeAll() } returns
        flowOf(listOf(place(id = "1", name = "Zeta"), place(id = "2", name = "Alpha")))
      viewModel = createViewModel()

      val state = viewModel.screenState.first()

      val ready = state.listState as ListState.Ready
      assertEquals(listOf("Alpha", "Zeta"), ready.places.map { it.name })
    }

  @Test
  fun `state updates reactively when the places Flow emits a new list`() =
    runTest {
      val placesFlow = MutableStateFlow<List<Place>>(emptyList())
      every { placeRepository.observeAll() } returns placesFlow
      viewModel = createViewModel()
      assertEquals(ListState.Empty, viewModel.screenState.first().listState)

      placesFlow.value = listOf(place(id = "1", name = "Home"))

      val ready = viewModel.screenState.first().listState as ListState.Ready
      assertEquals(listOf("1"), ready.places.map { it.id })
    }

  @Test
  fun `onSearchQueryChange updates the search query in state immediately`() =
    runTest {
      viewModel.onSearchQueryChange("Alpha")

      assertEquals("Alpha", viewModel.screenState.first().searchQuery)
    }

  @Test
  fun `onBackClicked posts MoveBack navigation event`() {
    viewModel.onBackClicked()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(PlacesViewModel.NavigationEvent.MoveBack, event)
  }

  @Test
  fun `onAddClick posts OpenEditPlace navigation event with empty id`() {
    viewModel.onAddClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(PlacesViewModel.NavigationEvent.OpenEditPlace(""), event)
  }

  @Test
  fun `onPlaceClick posts OpenEditPlace navigation event with the place id`() {
    viewModel.onPlaceClick("7")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(PlacesViewModel.NavigationEvent.OpenEditPlace("7"), event)
  }

  @Test
  fun `onPlaceMenuAction EDIT posts OpenEditPlace navigation event`() {
    viewModel.onPlaceMenuAction("7", PlaceMenuAction.EDIT)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(PlacesViewModel.NavigationEvent.OpenEditPlace("7"), event)
  }

  @Test
  fun `onPlaceMenuAction DELETE marks the place pending without confirmation or deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.place_deleted) } returns "Place deleted"

      viewModel.onPlaceMenuAction("7", PlaceMenuAction.DELETE)

      coVerify(exactly = 0) { deletePlaceUseCase(any()) }
      assertEquals(
        UndoEvent(batchKey = "7", message = "Place deleted", token = 1L),
        viewModel.undoDelete.undoEvents.first(),
      )
      assertEquals(null, viewModel.navigationEvent.value)
    }

  @Test
  fun `pending place is hidden from the list and restored on undo`() =
    runTest {
      every { placeRepository.observeAll() } returns
        flowOf(listOf(place(id = "1", name = "Home"), place(id = "2", name = "Work")))
      viewModel = createViewModel()
      viewModel.screenState.first()

      viewModel.onPlaceMenuAction("1", PlaceMenuAction.DELETE)
      val hidden = viewModel.screenState.first { state ->
        (state.listState as? ListState.Ready)?.places?.map { it.id } == listOf("2")
      }
      viewModel.undoDelete.undo("1")
      val restored = viewModel.screenState.first { state ->
        (state.listState as? ListState.Ready)?.places?.size == 2
      }

      assertEquals(listOf("2"), (hidden.listState as ListState.Ready).places.map { it.id })
      assertEquals(2, (restored.listState as ListState.Ready).places.size)
      coVerify(exactly = 0) { deletePlaceUseCase(any()) }
    }

  @Test
  fun `undo cancels the pending place delete`() =
    runTest {
      viewModel.onPlaceMenuAction("7", PlaceMenuAction.DELETE)

      viewModel.undoDelete.undo("7")

      coVerify(exactly = 0) { deletePlaceUseCase(any()) }
    }

  @Test
  fun `clearing the view model commits a pending place delete`() =
    runTest {
      viewModel.onPlaceMenuAction("7", PlaceMenuAction.DELETE)

      PlacesViewModel::class.java.getDeclaredMethod("onCleared").apply { isAccessible = true }.invoke(viewModel)

      coVerify(exactly = 1) { deletePlaceUseCase("7") }
    }

  @Test
  fun `onPlaceMenuAction SHARE shows an error toast when the place is not found`() =
    runTest {
      coEvery { placeRepository.getById("missing") } returns null

      viewModel.onPlaceMenuAction("missing", PlaceMenuAction.SHARE)

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(PlacesViewModel.NavigationEvent.ShowToast(R.string.error_sending), event)
    }

  @Test
  fun `onPlaceMenuAction SHARE shows an error toast when the backup file cannot be produced`() =
    runTest {
      val target = place(id = "1")
      coEvery { placeRepository.getById("1") } returns target
      coEvery { backupTool.placeToFile(target) } returns null

      viewModel.onPlaceMenuAction("1", PlaceMenuAction.SHARE)

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(PlacesViewModel.NavigationEvent.ShowToast(R.string.error_sending), event)
    }

  @Test
  fun `onPlaceMenuAction SHARE posts ShareFile navigation event when the backup file is ready`() =
    runTest {
      val target = place(id = "1", name = "Home")
      coEvery { placeRepository.getById("1") } returns target
      val tempFile = File.createTempFile("place", ".txt").apply { writeText("data") }
      tempFile.deleteOnExit()
      coEvery { backupTool.placeToFile(target) } returns tempFile
      every { intentFactory.createFileUriIntent(file = tempFile) } returns android.content.Intent()
      every { textProvider.getString(R.string.share_send_email) } returns "Send via email"

      viewModel.onPlaceMenuAction("1", PlaceMenuAction.SHARE)

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(PlacesViewModel.NavigationEvent.ShareFile::class, event!!::class)
      assertNotNull(event)
    }

  @Test
  fun `commit deletes the place exactly once after the undo window`() =
    runTest {
      viewModel.onPlaceMenuAction("1", PlaceMenuAction.DELETE)

      viewModel.undoDelete.commit("1")
      viewModel.undoDelete.commit("1")

      coVerify(exactly = 1) { deletePlaceUseCase("1") }
    }

  @Test
  fun `formats the place date when available`() =
    runTest {
      val target = place(id = "1", name = "Home")
      every { placeRepository.observeAll() } returns flowOf(listOf(target))
      every { dateTimeManager.getPlaceDateTimeFromGmt(target.dateTime) } returns LocalDate.of(2026, 7, 24)
      every { dateTimeManager.getDate(LocalDate.of(2026, 7, 24)) } returns "24 Jul 2026"
      viewModel = createViewModel()

      val state = viewModel.screenState.first()

      val ready = state.listState as ListState.Ready
      assertEquals("24 Jul 2026", ready.places.first().formattedDate)
    }
}
