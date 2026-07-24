package com.elementary.tasks.places.create

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.places.PlacesNavKey
import com.elementary.tasks.places.usecase.DeletePlaceUseCase
import com.elementary.tasks.places.usecase.SavePlaceUseCase
import com.elementary.tasks.simplemap.MarkerState
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class EditPlaceViewModelTest : BaseTest() {
  private val placeRepository = mockk<PlaceRepository>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val prefs = mockk<Prefs>(relaxed = true)
  private val intentDataReader = mockk<IntentDataReader>()
  private val deletePlaceUseCase = mockk<DeletePlaceUseCase>(relaxed = true)
  private val savePlaceUseCase = mockk<SavePlaceUseCase>(relaxed = true)

  private lateinit var viewModel: EditPlaceViewModel

  private fun place(
    id: String = "1",
    name: String = "Home",
    lat: Double = 12.0,
    lng: Double = 34.0,
  ) = Place(id = id, name = name, latitude = lat, longitude = lng, syncState = SyncState.Synced)

  private fun buildViewModel(
    id: String = "1",
    fromIntentData: Boolean = false,
  ) = EditPlaceViewModel(
    key = PlacesNavKey.Edit(id = id, fromIntentData = fromIntentData),
    dispatcherProvider = mockDispatcherProvider(),
    placeRepository = placeRepository,
    dateTimeManager = dateTimeManager,
    prefs = prefs,
    intentDataReader = intentDataReader,
    deletePlaceUseCase = deletePlaceUseCase,
    savePlaceUseCase = savePlaceUseCase,
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.markerStyle } returns 2
    every { prefs.radius } returns 75
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-07-24T10:00:00"
    coEvery { placeRepository.getById(any()) } returns null

    viewModel = buildViewModel()
  }

  @Test
  fun `loads marker style and radius from prefs`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(2, state.markerStyle)
      assertEquals(75, state.markerRadius)
    }

  @Test
  fun `loads existing place into state when found`() =
    runTest {
      coEvery { placeRepository.getById("1") } returns place(id = "1", name = "Home")

      val state = viewModel.state.first()

      assertEquals("Home", state.name)
      assertEquals(true, state.canDelete)
      assertEquals(true, state.canSave)
      assertEquals(1, state.markers.size)
    }

  @Test
  fun `keeps defaults when place is not found`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("", state.name)
      assertEquals(false, state.canDelete)
      assertEquals(false, state.canSave)
    }

  @Test
  fun `loads place from intent data and detects a matching entry in db`() =
    runTest {
      val fromFile = place(id = "9", name = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, Place::class.java) } returns fromFile
      coEvery { placeRepository.getById("9") } returns fromFile
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)

      val state = fileViewModel.state.first()

      assertEquals("From File", state.name)
      assertEquals(true, state.isFromFile)
      assertEquals(true, state.hasSameInDb)
    }

  @Test
  fun `loads place from intent data when no matching entry exists in db`() =
    runTest {
      val fromFile = place(id = "9", name = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, Place::class.java) } returns fromFile
      coEvery { placeRepository.getById("9") } returns null
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)

      val state = fileViewModel.state.first()

      assertEquals(true, state.isFromFile)
      assertEquals(false, state.hasSameInDb)
    }

  @Test
  fun `reloads the place on each fresh state collection`() =
    runTest {
      coEvery { placeRepository.getById("1") } returns place(id = "1")

      viewModel.state.first()
      viewModel.state.first()

      coVerify(exactly = 2) { placeRepository.getById("1") }
    }

  @Test
  fun `onNameChange updates name and clears the name error`() =
    runTest {
      viewModel.state.first()

      viewModel.onNameChange("New Place")

      assertEquals("New Place", viewModel.state.first().name)
      assertEquals(false, viewModel.state.first().nameError)
    }

  @Test
  fun `onSaveClick sets nameError when name is blank`() =
    runTest {
      viewModel.state.first()

      viewModel.onSaveClick()

      assertEquals(true, viewModel.state.first().nameError)
      coVerify(exactly = 0) { savePlaceUseCase(any()) }
    }

  @Test
  fun `onSaveClick emits NoLocationSelected when no marker was placed`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChange("Home")

      viewModel.onSaveClick()

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(EditPlaceViewModel.EditPlaceEvent.NoLocationSelected, event)
    }

  @Test
  fun `onSaveClick emits AskCopySaving when loaded from file and already in db`() =
    runTest {
      val fromFile = place(id = "9", name = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, Place::class.java) } returns fromFile
      coEvery { placeRepository.getById("9") } returns fromFile
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)
      fileViewModel.state.first()

      fileViewModel.onSaveClick()

      val event = fileViewModel.navigationEvent.value?.peekContent()
      assertEquals(EditPlaceViewModel.EditPlaceEvent.AskCopySaving, event)
    }

  @Test
  fun `onSaveClick saves the place and navigates back when valid`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChange("Home")
      viewModel.onMarkerPlaced(
        MarkerState(latLng = LatLng(12.0, 34.0), address = "Some address"),
      )

      viewModel.onSaveClick()

      coVerify(exactly = 1) { savePlaceUseCase(any()) }
      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(EditPlaceViewModel.EditPlaceEvent.MoveBack, event)
    }

  @Test
  fun `savePlace with newId assigns a fresh id different from the loaded place`() =
    runTest {
      coEvery { placeRepository.getById("1") } returns place(id = "1")
      viewModel.state.first()

      viewModel.savePlace(newId = true)

      val savedPlace = slot<Place>()
      coVerify(exactly = 1) { savePlaceUseCase(capture(savedPlace)) }
      assertNotEquals("1", savedPlace.captured.id)
    }

  @Test
  fun `onMarkerPlaced updates location fields and auto-fills empty name from address`() {
    // `state` re-runs loadInitial() in onStart on every fresh collection, which would reset
    // markerStyle/markerRadius back to the prefs defaults if we called `.first()` again to
    // inspect them. Observe through one persistent subscription instead of re-collecting.
    var latest = EditPlaceState()
    CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect { latest = it } }

    viewModel.onMarkerPlaced(
      MarkerState(latLng = LatLng(1.0, 2.0), address = "42 Main St", styleIndex = 4, radius = 90),
    )

    assertEquals(1.0, latest.lat, 0.0)
    assertEquals(2.0, latest.lng, 0.0)
    assertEquals("42 Main St", latest.address)
    assertEquals(4, latest.markerStyle)
    assertEquals(90, latest.markerRadius)
    assertEquals("42 Main St", latest.name)
    assertEquals(true, latest.canSave)
  }

  @Test
  fun `onMarkerPlaced does not overwrite an existing name`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChange("Custom Name")

      viewModel.onMarkerPlaced(MarkerState(latLng = LatLng(1.0, 2.0), address = "42 Main St"))

      assertEquals("Custom Name", viewModel.state.first().name)
    }

  @Test
  fun `onDeleteClick emits ConfirmDelete event`() {
    viewModel.onDeleteClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(EditPlaceViewModel.EditPlaceEvent.ConfirmDelete, event)
  }

  @Test
  fun `deletePlace deletes the place and navigates back`() =
    runTest {
      viewModel.deletePlace()

      coVerify(exactly = 1) { deletePlaceUseCase("1") }
      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(EditPlaceViewModel.EditPlaceEvent.MoveBack, event)
    }
}
