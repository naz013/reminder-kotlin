package com.elementary.tasks.places.list

import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.github.naz013.feature.reminder.util.BackupTool
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.places.usecase.DeletePlaceUseCase
import com.github.naz013.ui.map.MapStyle
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentFactory
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.PlaceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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
    coEvery { placeRepository.getAll() } returns emptyList()
    every { dateTimeManager.getPlaceDateTimeFromGmt(any()) } returns null

    viewModel =
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
  }

  @Test
  fun `loads empty state when there are no places`() =
    runTest {
      val state = viewModel.screenState.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `loads places sorted by name on first collection`() =
    runTest {
      coEvery { placeRepository.getAll() } returns listOf(place(id = "1", name = "Zeta"), place(id = "2", name = "Alpha"))

      val state = viewModel.screenState.first()

      val ready = state.listState as ListState.Ready
      assertEquals(listOf("Alpha", "Zeta"), ready.places.map { it.name })
    }

  @Test
  fun `reloads places on each fresh state collection`() =
    runTest {
      viewModel.screenState.first()
      viewModel.screenState.first()

      coVerify(atLeast = 3) { placeRepository.getAll() }
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
  fun `onPlaceMenuAction DELETE posts ConfirmDelete navigation event`() {
    viewModel.onPlaceMenuAction("7", PlaceMenuAction.DELETE)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(PlacesViewModel.NavigationEvent.ConfirmDelete("7"), event)
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
  fun `deletePlace delegates to the use case and reloads the list`() =
    runTest {
      viewModel.deletePlace("1")

      coVerify(exactly = 1) { deletePlaceUseCase("1") }
      coVerify(atLeast = 2) { placeRepository.getAll() }
    }

  @Test
  fun `formats the place date when available`() =
    runTest {
      val target = place(id = "1", name = "Home")
      coEvery { placeRepository.getAll() } returns listOf(target)
      every { dateTimeManager.getPlaceDateTimeFromGmt(target.dateTime) } returns LocalDate.of(2026, 7, 24)
      every { dateTimeManager.getDate(LocalDate.of(2026, 7, 24)) } returns "24 Jul 2026"

      val state = viewModel.screenState.first()

      val ready = state.listState as ListState.Ready
      assertEquals("24 Jul 2026", ready.places.first().formattedDate)
    }
}
