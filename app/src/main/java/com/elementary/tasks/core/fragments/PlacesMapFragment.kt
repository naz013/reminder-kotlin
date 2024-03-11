package com.elementary.tasks.core.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.network.PlacesApi
import com.elementary.tasks.core.network.places.PlacesResponse
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.os.colorOf
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.isVisible
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.FragmentPlacesMapBinding
import com.elementary.tasks.places.google.GooglePlaceItem
import com.elementary.tasks.places.google.GooglePlacesAdapter
import com.elementary.tasks.places.google.PlaceParser
import com.elementary.tasks.places.google.RequestBuilder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlacesMapFragment : BaseMapFragment<FragmentPlacesMapBinding>() {

  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val locationTracker by inject<LocationTracker> { parametersOf(locationListener) }

  private var mMap: GoogleMap? = null

  private var spinnerArray: MutableList<GooglePlaceItem> = mutableListOf()

  private var isZoom = true
  var isFullscreen = false
  private var isDark = false
  var markerRadius = -1
  private var markerStyle = -1
  private var mMarkerStyle: Drawable? = null
  private var mLat: Double = 0.0
  private var mLng: Double = 0.0

  private var locationListener: LocationTracker.Listener = object : LocationTracker.Listener {
    override fun onUpdate(lat: Double, lng: Double) {
      mLat = lat
      mLng = lng
    }
  }

  private var mMapListener: MapListener? = null
  private var mCallback: MapCallback? = null

  private var call: Call<PlacesResponse>? = null

  private val mMapCallback = OnMapReadyCallback { googleMap ->
    mMap = googleMap
    googleMap.uiSettings.isMyLocationButtonEnabled = false
    googleMap.uiSettings.isCompassEnabled = true
    setStyle(googleMap)
    tryToSetMyLocation()
    googleMap.setOnMapClickListener {
      hideLayers()
      hideStyles()
    }
    mCallback?.onMapReady()
  }
  private val mSearchCallback = object : Callback<PlacesResponse> {
    override fun onResponse(call: Call<PlacesResponse>, response: Response<PlacesResponse>) {
      if (response.code() == PlacesApi.OK) {
        val places = ArrayList<GooglePlaceItem>()
        for (place in response.body()?.results ?: listOf()) {
          places.add(PlaceParser.getDetails(place))
        }
        spinnerArray = places
        if (spinnerArray.size == 0) {
          toast(R.string.no_places_found)
        }
        addSelectAllItem()
        refreshAdapter()
      } else {
        toast(R.string.no_places_found)
      }
    }

    override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
      toast(R.string.no_places_found)
    }
  }

  val places: List<Place>
    get() {
      val places = ArrayList<Place>()
      if (spinnerArray.size > 0) {
        for (model in spinnerArray) {
          if (model.isSelected) {
            if (model.position != null) {
              places.add(
                Place(
                  radius = markerRadius,
                  marker = markerStyle,
                  latitude = model.latitude,
                  longitude = model.longitude,
                  name = model.name,
                  address = model.address,
                  tags = model.types,
                  dateTime = dateTimeManager.getNowGmtDateTime()
                )
              )
            }
          }
        }
      }
      return places
    }

  private val isLayersVisible: Boolean
    get() = binding.layersContainer.isVisible()

  private val isStylesVisible: Boolean
    get() = binding.mapStyleContainer.isVisible()

  fun setListener(listener: MapListener) {
    this.mMapListener = listener
  }

  fun setCallback(callback: MapCallback) {
    this.mCallback = callback
  }

  fun setMarkerStyle(markerStyle: Int) {
    this.markerStyle = markerStyle
  }

  private fun addMarker(
    pos: LatLng?,
    title: String?,
    clear: Boolean,
    animate: Boolean,
    radius: Int = markerRadius
  ) {
    var t = title
    if (mMap != null && pos != null) {
      if (pos.latitude == 0.0 && pos.longitude == 0.0) return
      markerRadius = radius
      if (markerRadius == -1) {
        markerRadius = prefs.radius
      }
      if (clear) {
        mMap?.clear()
      }
      if (t == null || t.matches("".toRegex())) {
        t = pos.toString()
      }
      mMap?.addMarker(
        MarkerOptions()
          .position(pos)
          .title(t)
          .icon(BitmapUtils.getDescriptor(mMarkerStyle!!))
          .draggable(clear)
      )
      val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
      val strokeWidth = 3f
      mMap?.addCircle(
        CircleOptions()
          .center(pos)
          .radius(markerRadius.toDouble())
          .strokeWidth(strokeWidth)
          .fillColor(colorOf(marker.fillColor))
          .strokeColor(colorOf(marker.strokeColor))
      )
      if (animate) {
        animate(pos)
      }
    }
  }

  fun recreateMarker(radius: Int = markerRadius) {
    markerRadius = radius
    if (markerRadius == -1) {
      markerRadius = prefs.radius
    }
    if (mMap != null) {
      addMarkers()
    }
  }

  private fun recreateStyle(style: Int) {
    markerStyle = style
    createStyleDrawable()
    if (mMap != null) {
      addMarkers()
    }
  }

  private fun addSelectAllItem() {
    if (spinnerArray.size > 1) {
      spinnerArray.add(
        GooglePlaceItem(
          getString(R.string.add_all),
          "",
          "",
          "",
          null,
          listOf(),
          false
        )
      )
    }
  }

  fun selectMarkers(list: List<Place>) {
    mMap?.clear()
    toModels(list, true)
    refreshAdapter()
  }

  fun animate(latLng: LatLng?) {
    latLng?.also {
      val update = CameraUpdateFactory.newLatLngZoom(it, 13f)
      mMap?.animateCamera(update)
    }
  }

  fun onBackPressed(): Boolean {
    return when {
      isLayersVisible -> {
        hideLayers()
        false
      }

      isStylesVisible -> {
        hideStyles()
        false
      }

      else -> true
    }
  }

  private fun initArgs() {
    val args = arguments
    if (args != null) {
      isZoom = args.getBoolean(ENABLE_ZOOM, true)
      isDark = args.getBoolean(THEME_MODE, false)
      markerStyle = args.getInt(MARKER_STYLE, prefs.markerStyle)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initArgs()
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentPlacesMapBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    markerRadius = prefs.radius
    isDark = themeUtil.isDark

    binding.mapView.onCreate(savedInstanceState)
    binding.mapView.getMapAsync(mMapCallback)

    initViews()
    createStyleDrawable()

    binding.cardSearch.setOnEditorActionListener { _, actionId, event ->
      if (
        event != null && event.keyCode == KeyEvent.KEYCODE_ENTER ||
        actionId == EditorInfo.IME_ACTION_NEXT
      ) {
        hideKeyboard()
        loadPlaces()
        return@setOnEditorActionListener true
      }
      false
    }
  }

  private fun showStyleDialog() {
    dialogues.showColorBottomDialog(
      requireActivity(),
      prefs.markerStyle,
      ThemeProvider.colorsForSlider(requireActivity())
    ) {
      prefs.markerStyle = it
      recreateStyle(it)
    }
  }

  private fun showRadiusDialog() {
    val radiusFormatter = DefaultRadiusFormatter(requireContext(), prefs.useMetric)
    dialogues.showRadiusBottomDialog(requireActivity(), markerRadius) {
      recreateMarker(it)
      mMapListener?.onRadiusChanged(it)
      return@showRadiusBottomDialog radiusFormatter.format(it)
    }
  }

  private fun createStyleDrawable() {
    mMarkerStyle = DrawableHelper.withContext(requireContext())
      .withDrawable(R.drawable.ic_fluent_place)
      .withColor(themeUtil.getMarkerLightColor(markerStyle))
      .tint()
      .get()
  }

  private fun initViews() {
    binding.placesList.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    LinearSnapHelper().attachToRecyclerView(binding.placesList)

    binding.placesListCard.visibility = View.GONE
    binding.layersContainer.visibility = View.GONE

    binding.zoomCard.setOnClickListener { zoomClick() }
    binding.layersCard.setOnClickListener { toggleLayers() }
    binding.markersCard.setOnClickListener { toggleMarkers() }
    binding.radiusCard.setOnClickListener { toggleRadius() }
    binding.cardClear.setOnClickListener { loadPlaces() }
    binding.backCard.setOnClickListener { invokeBack() }

    binding.typeNormal.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_NORMAL) }
    binding.typeSatellite.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_SATELLITE) }
    binding.typeHybrid.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_HYBRID) }
    binding.typeTerrain.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_TERRAIN) }

    binding.styleDay.setOnClickListener { styleClick(0) }
    binding.styleRetro.setOnClickListener { styleClick(1) }
    binding.styleSilver.setOnClickListener { styleClick(2) }
    binding.styleNight.setOnClickListener { styleClick(3) }
    binding.styleDark.setOnClickListener { styleClick(4) }
    binding.styleAubergine.setOnClickListener { styleClick(5) }

    if (!Module.isPro) {
      binding.markersCard.visibility = View.GONE
    }
    if (!isZoom) {
      binding.zoomCard.visibility = View.GONE
    }
    hideStyles()
    hideLayers()
  }

  private fun typeClick(type: Int) {
    val map = mMap ?: return
    setMapType(map, type) {
      hideLayers()
      if (type == GoogleMap.MAP_TYPE_NORMAL) {
        showStyles()
      }
    }
  }

  private fun styleClick(style: Int) {
    prefs.mapStyle = style
    val map = mMap ?: return
    refreshStyles(map)
    hideStyles()
  }

  fun invokeBack() {
    restoreScaleButton()
    mMapListener?.onBackClick()
  }

  private fun hideKeyboard() {
    val imm = systemServiceProvider.provideInputMethodManager()
    imm?.hideSoftInputFromWindow(binding.cardSearch.windowToken, 0)
  }

  private fun tryToSetMyLocation() {
    permissionFlow.askPermissions(
      listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION),
      { setMyLocation() }
    ) { toast(R.string.cant_access_location_services) }
  }

  @SuppressLint("MissingPermission")
  private fun setMyLocation() {
    if (Permissions.checkPermission(
        requireContext(),
        Permissions.ACCESS_COARSE_LOCATION,
        Permissions.ACCESS_FINE_LOCATION
      )
    ) {
      mMap?.isMyLocationEnabled = true
    }
  }

  private fun loadPlaces() {
    val req = binding.cardSearch.text.toString().trim().lowercase()
    if (req.matches("".toRegex())) return
    cancelSearchTask()
    call = RequestBuilder.getSearch(req)
    if (mLat != 0.0 && mLng != 0.0) {
      call = RequestBuilder.getNearby(mLat, mLng, req)
    }
    call?.enqueue(mSearchCallback)
  }

  private fun cancelSearchTask() {
    call?.cancel()?.takeIf { call?.isExecuted == false }
  }

  private fun refreshAdapter() {
    val placesAdapter = GooglePlacesAdapter()
    placesAdapter.setPlaces(spinnerArray)
    placesAdapter.setEventListener(object : SimpleListener {
      override fun onItemClicked(position: Int, view: View) {
        hideLayers()
        animate(spinnerArray[position].position)
      }

      override fun onItemLongClicked(position: Int, view: View) {
      }
    })
    if (spinnerArray.size > 0) {
      binding.placesListCard.visible()
      binding.placesList.adapter = placesAdapter
      addMarkers()
    } else {
      binding.placesListCard.gone()
    }
  }

  private fun toModels(list: List<Place>?, select: Boolean) {
    spinnerArray = ArrayList()
    if (!list.isNullOrEmpty()) {
      for (model in list) {
        spinnerArray.add(
          GooglePlaceItem(
            name = model.name,
            id = model.id,
            icon = "",
            address = model.address,
            position = LatLng(
              model.latitude,
              model.longitude
            ),
            types = model.tags,
            selected = select
          )
        )
      }
    }
  }

  private fun addMarkers() {
    mMap?.clear()
    if (spinnerArray.size > 0) {
      for (model in spinnerArray) {
        addMarker(model.position, model.name, clear = false, animate = false, radius = markerRadius)
      }
    }
  }

  private fun showStyles() {
    binding.mapStyleContainer.visible()
  }

  private fun toggleRadius() {
    if (isLayersVisible) {
      hideLayers()
    }
    if (isStylesVisible) {
      hideStyles()
    }
    showRadiusDialog()
  }

  private fun toggleMarkers() {
    if (isLayersVisible) {
      hideLayers()
    }
    if (isStylesVisible) {
      hideStyles()
    }
    showStyleDialog()
  }

  private fun toggleLayers() {
    when {
      isLayersVisible -> hideLayers()
      isStylesVisible -> hideStyles()
      else -> binding.layersContainer.visible()
    }
  }

  private fun hideStyles() {
    if (isStylesVisible) {
      binding.mapStyleContainer.gone()
    }
  }

  private fun hideLayers() {
    if (isLayersVisible) {
      binding.layersContainer.gone()
    }
  }

  private fun zoomClick() {
    isFullscreen = !isFullscreen
    mMapListener?.onZoomClick(isFullscreen)
    if (isFullscreen) {
      binding.zoomIcon.setImageResource(R.drawable.ic_fluent_small)
    } else {
      restoreScaleButton()
    }
  }

  private fun restoreScaleButton() {
    binding.zoomIcon.setImageResource(R.drawable.ic_builder_map_full_screen)
  }

  override fun onResume() {
    binding.mapView.onResume()
    super.onResume()
    tryToStartTracking()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    binding.mapView.onLowMemory()
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.mapView.onDestroy()
    cancelTracking()
  }

  override fun onPause() {
    super.onPause()
    binding.mapView.onPause()
    cancelTracking()
  }

  override fun onStop() {
    super.onStop()
    binding.mapView.onStop()
    cancelTracking()
  }

  private fun tryToStartTracking() {
    permissionFlow.askPermissions(
      listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION),
      { startTracking() }
    ) { toast(R.string.cant_access_location_services) }
  }

  private fun startTracking() {
    locationTracker.startUpdates()
  }

  private fun cancelTracking() {
    locationTracker.removeUpdates()
  }

  override fun onDetach() {
    super.onDetach()
    cancelTracking()
    cancelSearchTask()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    cancelTracking()
  }

  companion object {

    const val ENABLE_ZOOM = "enable_zoom"
    const val MARKER_STYLE = "marker_style"
    const val THEME_MODE = "theme_mode"

    fun newInstance(isZoom: Boolean, isDark: Boolean): PlacesMapFragment {
      val fragment = PlacesMapFragment()
      val args = Bundle()
      args.putBoolean(ENABLE_ZOOM, isZoom)
      args.putBoolean(THEME_MODE, isDark)
      fragment.arguments = args
      return fragment
    }

    fun newInstance(isZoom: Boolean, markerStyle: Int, isDark: Boolean): PlacesMapFragment {
      val fragment = PlacesMapFragment()
      val args = Bundle()
      args.putBoolean(ENABLE_ZOOM, isZoom)
      args.putBoolean(THEME_MODE, isDark)
      args.putInt(MARKER_STYLE, markerStyle)
      fragment.arguments = args
      return fragment
    }
  }
}
