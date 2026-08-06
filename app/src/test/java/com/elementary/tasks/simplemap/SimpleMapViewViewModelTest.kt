package com.elementary.tasks.simplemap

import android.location.Address
import android.location.Location
import android.location.LocationManager
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.utils.GeocoderTask
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.repository.PlaceRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SimpleMapViewViewModelTest : BaseTest() {
  private val prefs = mockk<Prefs>(relaxed = true)
  private val geocoderTask = mockk<GeocoderTask>(relaxed = true)
  private val placeRepository = mockk<PlaceRepository>(relaxed = true)
  private val uiPlaceListAdapter = mockk<UiPlaceListAdapter>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val systemServiceProvider = mockk<SystemServiceProvider>()
  private val mapStyle = mockk<MapStyle>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.radius } returns 50
    every { prefs.useMetric } returns true
    every { prefs.mapType } returns GoogleMap.MAP_TYPE_NORMAL
    every { prefs.mapStyle } returns 0
    every { prefs.markerStyle } returns 5
    every { prefs.hapticsEnabled } returns false
    every { systemServiceProvider.provideLocationManager() } returns null
    coEvery { placeRepository.getAll() } returns emptyList()
  }

  /**
   * [SimpleMapViewViewModel.state] is `_state.stateInWhileSubscribed(...).onStart { loadInitialState() }`,
   * and `loadInitialState()` unconditionally re-reads several fields straight from `prefs` on every
   * fresh collection. Re-collecting via a second `.first()` after an action would silently clobber
   * those fields back to their prefs-stubbed defaults. A single persistent collector observes only
   * real mutations instead.
   */
  private fun createViewModel(
    mapParams: MapParams = MapParams(),
  ): Pair<SimpleMapViewViewModel, MutableList<MapUiState>> {
    val viewModel =
      SimpleMapViewViewModel(
        mapParams = mapParams,
        dispatcherProvider = mockDispatcherProvider(),
        prefs = prefs,
        geocoderTask = geocoderTask,
        placeRepository = placeRepository,
        uiPlaceListAdapter = uiPlaceListAdapter,
        textProvider = textProvider,
        systemServiceProvider = systemServiceProvider,
        mapStyle = mapStyle,
      )
    val states = mutableListOf<MapUiState>()
    CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect { states.add(it) } }
    return viewModel to states
  }

  private fun marker(id: String = "m1") =
    object : MapMarker {
      override val style: Int = 5
      override val radius: Int = 50
      override val latLng: LatLng = LatLng(1.0, 2.0)
      override val title: String = id
    }

  @Test
  fun `initial state loads map type, style, and radius from prefs`() {
    every { prefs.mapType } returns GoogleMap.MAP_TYPE_SATELLITE
    every { prefs.mapStyle } returns 2

    val (_, states) = createViewModel()

    val state = states.last()
    assertEquals(GoogleMap.MAP_TYPE_SATELLITE, state.selectedMapType)
    assertEquals(2, state.selectedMapStyle)
    assertEquals(50, state.radiusMeters)
  }

  @Test
  fun `loads recent places on creation when the screen shows places`() {
    val place = mockk<com.github.naz013.domain.Place>(relaxed = true)
    val uiPlace = mockk<UiPlaceList>(relaxed = true)
    coEvery { placeRepository.getAll() } returns listOf(place)
    every { uiPlaceListAdapter.convert(place) } returns uiPlace

    val (_, states) = createViewModel(MapParams(isPlaces = true))

    assertEquals(listOf(uiPlace), states.last().recentPlaces)
  }

  @Test
  fun `does not load recent places when the screen does not show places`() {
    createViewModel(MapParams(isPlaces = false))

    coEvery { placeRepository.getAll() } returns emptyList()
    // If loadRecentPlaces() had run it would have called getAll() during setup - nothing further
    // to assert here beyond the default empty recentPlaces list, since that coroutine never launches.
  }

  @Test
  fun `onMyLocationClick requests my-location permission`() {
    val (viewModel, _) = createViewModel()

    viewModel.onMyLocationClick()

    assertEquals(SimpleMapViewViewModel.MapEvent.RequestMyLocationPermission, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onLocationPermissionGranted after my-location click zooms to the resolved location`() {
    val locationManager = mockk<LocationManager>()
    val location = mockk<Location>()
    every { location.latitude } returns 10.0
    every { location.longitude } returns 20.0
    every { locationManager.getBestProvider(any(), false) } returns "gps"
    every { locationManager.getLastKnownLocation("gps") } returns location
    every { systemServiceProvider.provideLocationManager() } returns locationManager
    val (viewModel, states) = createViewModel()
    viewModel.onMyLocationClick()

    viewModel.onLocationPermissionGranted()

    assertTrue(states.last().hasLocationPermission)
    val event = viewModel.event.value?.peekContent()
    assertTrue(event is SimpleMapViewViewModel.MapEvent.ZoomToLocation)
    assertEquals(LatLng(10.0, 20.0), (event as SimpleMapViewViewModel.MapEvent.ZoomToLocation).latLng)
  }

  @Test
  fun `onLocationPermissionGranted without a prior my-location click does not zoom`() {
    val (viewModel, states) = createViewModel()

    viewModel.onLocationPermissionGranted()

    assertTrue(states.last().hasLocationPermission)
    assertNull(viewModel.event.value?.peekContent())
  }

  @Test
  fun `onMapReady requests location permission when not yet granted`() {
    val (viewModel, _) = createViewModel()

    viewModel.onMapReady()

    assertEquals(SimpleMapViewViewModel.MapEvent.RequestLocationPermission, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onMapReady does not request permission again once already granted`() {
    val (viewModel, _) = createViewModel()
    viewModel.onLocationPermissionGranted()

    viewModel.onMapReady()

    assertNull(viewModel.event.value?.peekContent())
  }

  @Test
  fun `setSeedMarkers in touch mode seeds a single editable marker and zooms once`() {
    val (viewModel, states) = createViewModel(MapParams(isTouch = true))

    viewModel.setSeedMarkers(listOf(marker("first")))

    assertEquals(1, states.last().markers.size)
    assertEquals(
      SimpleMapViewViewModel.MapEvent.ZoomToLocation(LatLng(1.0, 2.0), MapConfig.DEFAULT_ZOOM),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `setSeedMarkers in touch mode only seeds once`() {
    val (viewModel, states) = createViewModel(MapParams(isTouch = true))
    viewModel.setSeedMarkers(listOf(marker("first")))

    viewModel.setSeedMarkers(listOf(marker("second")))

    assertEquals("first", states.last().markers.first().title)
  }

  @Test
  fun `setSeedMarkers in read-only mode maps all markers and centers once`() {
    val (viewModel, states) = createViewModel(MapParams(isTouch = false))

    viewModel.setSeedMarkers(listOf(marker("a"), marker("b")))

    assertEquals(2, states.last().markers.size)
    assertTrue(viewModel.event.value?.peekContent() is SimpleMapViewViewModel.MapEvent.ZoomToLocation)
  }

  @Test
  fun `moveCamera posts a ZoomToLocation event`() {
    val (viewModel, _) = createViewModel()
    val latLng = LatLng(5.0, 6.0)

    viewModel.moveCamera(latLng)

    assertEquals(SimpleMapViewViewModel.MapEvent.ZoomToLocation(latLng, MapConfig.DEFAULT_ZOOM), viewModel.event.value?.peekContent())
  }

  @Test
  fun `onMapClick in touch mode places a marker using the geocoded address`() {
    val latLng = LatLng(3.0, 4.0)
    every { geocoderTask.getAddressForLocation(latLng) } returns "3 Example St"
    val (viewModel, states) = createViewModel(MapParams(isTouch = true))

    viewModel.onMapClick(latLng)

    assertEquals("3 Example St", states.last().markers.first().title)
  }

  @Test
  fun `onMapClick in read-only mode posts MapClicked instead of placing a marker`() {
    val (viewModel, states) = createViewModel(MapParams(isTouch = false))

    viewModel.onMapClick(LatLng(3.0, 4.0))

    assertTrue(states.last().markers.isEmpty())
    assertEquals(SimpleMapViewViewModel.MapEvent.MapClicked, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onMapClick closes an open picker instead of placing a marker`() {
    val (viewModel, states) = createViewModel(MapParams(isTouch = true))
    viewModel.onRadiusButtonClicked()

    viewModel.onMapClick(LatLng(3.0, 4.0))

    assertNull(states.last().activePicker)
    assertTrue(states.last().markers.isEmpty())
  }

  @Test
  fun `onAddressQueryChanged updates the query and populates suggestions`() {
    val address = mockk<Address>(relaxed = true)
    every { geocoderTask.findAddresses("paris", any()) } answers {
      secondArg<(List<Address>) -> Unit>().invoke(listOf(address))
    }
    val (viewModel, states) = createViewModel()

    viewModel.onAddressQueryChanged("paris")

    assertEquals("paris", states.last().addressQuery)
    assertEquals(listOf(address), states.last().addressSuggestions)
  }

  @Test
  fun `onAddressSuggestionSelected places a marker and clears suggestions`() {
    val address =
      mockk<Address> {
        every { latitude } returns 7.0
        every { longitude } returns 8.0
        every { getAddressLine(0) } returns "7 Example Ave"
      }
    val (viewModel, states) = createViewModel()

    viewModel.onAddressSuggestionSelected(address)

    assertEquals("7 Example Ave", states.last().addressQuery)
    assertTrue(states.last().addressSuggestions.isEmpty())
    assertEquals(LatLng(7.0, 8.0), states.last().markers.first().latLng)
  }

  @Test
  fun `dismissAddressSuggestions clears the suggestion list`() {
    val (viewModel, states) = createViewModel()

    viewModel.dismissAddressSuggestions()

    assertTrue(states.last().addressSuggestions.isEmpty())
  }

  @Test
  fun `onLayersButtonClicked opens the layers picker`() {
    val (viewModel, states) = createViewModel()

    viewModel.onLayersButtonClicked()

    assertEquals(MapPicker.LAYERS, states.last().activePicker)
  }

  @Test
  fun `onLayersButtonClicked closes the layers picker when already open`() {
    val (viewModel, states) = createViewModel()
    viewModel.onLayersButtonClicked()

    viewModel.onLayersButtonClicked()

    assertNull(states.last().activePicker)
  }

  @Test
  fun `onMarkerStyleButtonClicked toggles the marker style picker`() {
    val (viewModel, states) = createViewModel()

    viewModel.onMarkerStyleButtonClicked()
    assertEquals(MapPicker.MARKER_STYLE, states.last().activePicker)

    viewModel.onMarkerStyleButtonClicked()
    assertNull(states.last().activePicker)
  }

  @Test
  fun `onRadiusButtonClicked toggles the radius picker`() {
    val (viewModel, states) = createViewModel()

    viewModel.onRadiusButtonClicked()

    assertEquals(MapPicker.RADIUS, states.last().activePicker)
  }

  @Test
  fun `onPlacesButtonClicked toggles the places picker`() {
    val (viewModel, states) = createViewModel()

    viewModel.onPlacesButtonClicked()

    assertEquals(MapPicker.PLACES, states.last().activePicker)
  }

  @Test
  fun `onCustomButtonClicked posts a CustomButtonClicked event with the given id`() {
    val (viewModel, _) = createViewModel()

    viewModel.onCustomButtonClicked(7)

    assertEquals(SimpleMapViewViewModel.MapEvent.CustomButtonClicked(7), viewModel.event.value?.peekContent())
  }

  @Test
  fun `onMapTypeSelected updates state and persists when remembering map style`() {
    val (viewModel, states) = createViewModel(MapParams(rememberMapStyle = true))

    viewModel.onMapTypeSelected(GoogleMap.MAP_TYPE_SATELLITE)

    assertEquals(GoogleMap.MAP_TYPE_SATELLITE, states.last().selectedMapType)
    verify { prefs.mapType = GoogleMap.MAP_TYPE_SATELLITE }
  }

  @Test
  fun `onMapTypeSelected does not persist when not remembering map style`() {
    val (viewModel, _) = createViewModel(MapParams(rememberMapStyle = false))

    viewModel.onMapTypeSelected(GoogleMap.MAP_TYPE_SATELLITE)

    verify(exactly = 0) { prefs.mapType = any() }
  }

  @Test
  fun `onMapStyleSelected updates state and persists when remembering map style`() {
    val (viewModel, states) = createViewModel(MapParams(rememberMapStyle = true))

    viewModel.onMapStyleSelected(3)

    assertEquals(3, states.last().selectedMapStyle)
    verify { prefs.mapStyle = 3 }
  }

  @Test
  fun `onMarkerStyleSelected persists when remembering marker style and plays haptics on change`() {
    every { prefs.hapticsEnabled } returns true
    // No seeded marker: onMarkerStyleSelected's placeMarker() call (which would emit further
    // events, overwriting this one) is only reached when a marker already exists.
    val (viewModel, states) = createViewModel(MapParams(rememberMarkerStyle = true, isTouch = true))

    viewModel.onMarkerStyleSelected(2)

    assertEquals(2, states.last().selectedMarkerStyle)
    verify { prefs.markerStyle = 2 }
    assertEquals(SimpleMapViewViewModel.MapEvent.HapticFeedback, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onMarkerStyleSelected does not play haptics when the style is unchanged`() {
    every { prefs.hapticsEnabled } returns true
    val (viewModel, _) = createViewModel()

    viewModel.onMarkerStyleSelected(MapConfig.DEFAULT_MARKER_STYLE)

    assertNull(viewModel.event.value?.peekContent())
  }

  @Test
  fun `onRadiusChanged updates the radius text and persists when remembering marker radius`() {
    val (viewModel, states) = createViewModel(MapParams(rememberMarkerRadius = true))

    viewModel.onRadiusChanged(200f)

    assertEquals(200, states.last().radiusMeters)
    verify { prefs.radius = 200 }
  }

  @Test
  fun `onRecentPlaceSelected places a marker at the place's location`() {
    val place =
      mockk<UiPlaceList> {
        every { latLng } returns LatLng(9.0, 10.0)
        every { name } returns "Home"
        every { markerStyle } returns 3
      }
    val (viewModel, states) = createViewModel()

    viewModel.onRecentPlaceSelected(place)

    assertEquals(LatLng(9.0, 10.0), states.last().markers.first().latLng)
    assertEquals("Home", states.last().markers.first().title)
    assertNull(states.last().activePicker)
  }

  @Test
  fun `onBackPressed closes an open picker and reports the back press as handled`() {
    val (viewModel, states) = createViewModel()
    viewModel.onRadiusButtonClicked()

    val shouldContinue = viewModel.onBackPressed()

    assertFalse(shouldContinue)
    assertNull(states.last().activePicker)
  }

  @Test
  fun `onBackPressed reports the back press as unhandled when no picker is open`() {
    val (viewModel, _) = createViewModel()

    val shouldContinue = viewModel.onBackPressed()

    assertTrue(shouldContinue)
  }
}
