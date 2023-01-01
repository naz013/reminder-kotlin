package com.elementary.tasks.core.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Criteria
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.elementary.tasks.core.view_models.places.PlacesViewModel
import com.elementary.tasks.core.views.AddressAutoCompleteView
import com.elementary.tasks.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AdvancedMapFragment : BaseMapFragment<FragmentMapBinding>() {

  private val viewModel by viewModel<PlacesViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val dateTimeManager by inject<DateTimeManager>()

  private var mMap: GoogleMap? = null

  private var placeRecyclerAdapter = RecentPlacesAdapter(currentStateHolder, dateTimeManager)

  private var isTouch = true
  private var isZoom = true
  private var isBack = true
  private var isStyles = true
  private var isPlaces = true
  private var isSearch = true
  private var isRadius = true
  var isFullscreen = false
  private var isDark = false
  private var markerTitle: String = ""
  var markerRadius = -1
  var markerStyle = -1
    private set
  private var mMarkerStyle: Drawable? = null
  private var lastPos: LatLng? = null
  private val strokeWidth = 3f

  private var mListener: MapListener? = null
  private var mCallback: MapCallback? = null

  private var onMapClickListener: GoogleMap.OnMapClickListener? = null
  private var onMarkerClickListener: GoogleMap.OnMarkerClickListener? = null

  private val mMapCallback = OnMapReadyCallback { googleMap ->
    mMap = googleMap
    googleMap.uiSettings.isMyLocationButtonEnabled = false
    googleMap.uiSettings.isCompassEnabled = true
    setStyle(googleMap)
    tryToSetMyLocation()
    googleMap.setOnMapClickListener {
      hideLayers()
      hideStyles()
      onMapClickListener?.onMapClick(it)
    }
    setOnMarkerClick(onMarkerClickListener)
    if (lastPos != null) {
      addMarker(lastPos, lastPos.toString(), clear = true, animate = false, radius = markerRadius)
    }
    mCallback?.onMapReady()
  }

  private val isLayersVisible: Boolean
    get() = binding.layersContainer.visibility == View.VISIBLE

  private val isStylesVisible: Boolean
    get() = binding.mapStyleContainer.visibility == View.VISIBLE

  fun setSearchEnabled(enabled: Boolean) {
    if (enabled) {
      binding.searchCard.visibility = View.VISIBLE
    } else {
      binding.searchCard.visibility = View.GONE
    }
  }

  fun setListener(listener: MapListener) {
    this.mListener = listener
  }

  fun setCallback(callback: MapCallback) {
    this.mCallback = callback
  }

  fun addMarker(pos: LatLng?, title: String?, clear: Boolean, animate: Boolean, radius: Int = markerRadius) {
    var t = title
    if (mMap != null && pos != null) {
      markerRadius = radius
      if (markerRadius == -1)
        markerRadius = prefs.radius
      if (clear) mMap?.clear()
      if (t == null || t == "") t = pos.toString()
      if (!Module.isPro && markerStyle != DEF_MARKER_STYLE) {
        markerStyle = DEF_MARKER_STYLE
        createStyleDrawable()
      }
      lastPos = pos
      mListener?.placeChanged(pos, t)
      mMap?.addMarker(MarkerOptions()
        .position(pos)
        .title(t)
        .icon(BitmapUtils.getDescriptor(mMarkerStyle!!))
        .draggable(clear))
      val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
      mMap?.addCircle(CircleOptions()
        .center(pos)
        .radius(markerRadius.toDouble())
        .strokeWidth(strokeWidth)
        .fillColor(colorOf(marker.fillColor))
        .strokeColor(colorOf(marker.strokeColor)))
      if (animate) animate(pos)
    }
  }

  fun addMarker(pos: LatLng, title: String?, clear: Boolean, markerStyle: Int, animate: Boolean, radius: Int): Boolean {
    var t = title
    if (mMap != null) {
      markerRadius = radius
      if (markerRadius == -1) {
        markerRadius = prefs.radius
      }
      if (!Module.isPro && markerStyle != DEF_MARKER_STYLE) {
        this.markerStyle = DEF_MARKER_STYLE
      } else {
        this.markerStyle = markerStyle
      }
      createStyleDrawable()
      if (clear) mMap?.clear()
      if (t == null || t.matches("".toRegex())) t = pos.toString()
      lastPos = pos
      mListener?.placeChanged(pos, t)
      mMap?.addMarker(MarkerOptions()
        .position(pos)
        .title(t)
        .icon(BitmapUtils.getDescriptor(mMarkerStyle!!))
        .draggable(clear))
      val marker = themeUtil.getMarkerRadiusStyle(this.markerStyle)
      mMap?.addCircle(CircleOptions()
        .center(pos)
        .radius(markerRadius.toDouble())
        .strokeWidth(strokeWidth)
        .fillColor(colorOf(marker.fillColor))
        .strokeColor(colorOf(marker.strokeColor)))
      if (animate) animate(pos)
      return true
    } else {
      Timber.d("Map is not initialized!")
      return false
    }
  }

  fun recreateMarker(radius: Int = markerRadius) {
    markerRadius = radius
    if (markerRadius == -1)
      markerRadius = prefs.radius
    if (mMap != null && lastPos != null) {
      mMap?.clear()
      if (markerTitle == "" || markerTitle.matches("".toRegex()))
        markerTitle = lastPos!!.toString()
      mListener?.placeChanged(lastPos!!, markerTitle)
      if (!Module.isPro && markerStyle != DEF_MARKER_STYLE) {
        markerStyle = DEF_MARKER_STYLE
        createStyleDrawable()
      }
      mMap?.addMarker(MarkerOptions()
        .position(lastPos!!)
        .title(markerTitle)
        .icon(BitmapUtils.getDescriptor(mMarkerStyle!!))
        .draggable(true))
      val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
      mMap?.addCircle(CircleOptions()
        .center(lastPos!!)
        .radius(markerRadius.toDouble())
        .strokeWidth(strokeWidth)
        .fillColor(colorOf(marker.fillColor))
        .strokeColor(colorOf(marker.strokeColor)))
      animate(lastPos!!)
    }
  }

  private fun recreateStyle(style: Int) {
    markerStyle = style
    createStyleDrawable()
    if (mMap != null && lastPos != null) {
      mMap?.clear()
      if (markerTitle == "" || markerTitle.matches("".toRegex()))
        markerTitle = lastPos.toString()
      mListener?.placeChanged(lastPos!!, markerTitle)
      if (!Module.isPro && markerStyle != DEF_MARKER_STYLE) {
        markerStyle = DEF_MARKER_STYLE
        createStyleDrawable()
      }
      mMap?.addMarker(MarkerOptions()
        .position(lastPos!!)
        .title(markerTitle)
        .icon(BitmapUtils.getDescriptor(mMarkerStyle!!))
        .draggable(true))
      if (markerStyle >= 0) {
        val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
        if (markerRadius == -1) {
          markerRadius = prefs.radius
        }
        mMap?.addCircle(CircleOptions()
          .center(lastPos!!)
          .radius(markerRadius.toDouble())
          .strokeWidth(strokeWidth)
          .fillColor(colorOf(marker.fillColor))
          .strokeColor(colorOf(marker.strokeColor)))
      }
      animate(lastPos!!)
    }
  }

  private fun createStyleDrawable() {
    mMarkerStyle = DrawableHelper.withContext(requireContext())
      .withDrawable(R.drawable.ic_twotone_place_24px)
      .withColor(themeUtil.getMarkerLightColor(markerStyle))
      .tint()
      .get()
  }

  fun setStyle(style: Int) {
    this.markerStyle = style
  }

  fun moveCamera(pos: LatLng, i1: Int, i2: Int, i3: Int, i4: Int) {
    if (mMap != null) {
      animate(pos)
      mMap?.setPadding(i1, i2, i3, i4)
    }
  }

  private fun animate(latLng: LatLng) {
    val update = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
    mMap?.animateCamera(update)
  }

  private fun tryToMoveToMyLocation() {
    permissionFlow.askPermissions(
      listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION),
      { moveToMyLocation() }
    ) { toast(R.string.cant_access_location_services) }
  }

  @Suppress("DEPRECATION")
  private fun moveToMyLocation() {
    if (!Permissions.checkPermission(
        requireContext(),
        Permissions.ACCESS_COARSE_LOCATION,
        Permissions.ACCESS_FINE_LOCATION
      )
    ) {
      return
    }
    mMap?.run {
      val locationManager = systemServiceProvider.provideLocationManager()
      val criteria = Criteria()
      var location: Location? = null
      try {
        location = locationManager?.getLastKnownLocation(locationManager.getBestProvider(criteria, false)
          ?: "")
      } catch (e: Throwable) {
        Timber.d("moveToMyLocation: ${e.message}")
      }

      if (location != null) {
        val pos = LatLng(location.latitude, location.longitude)
        animate(pos)
      } else {
        runCatching {
          val pos = LatLng(myLocation.latitude, myLocation.longitude)
          animate(pos)
        }
      }
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
      isTouch = args.getBoolean(ENABLE_TOUCH, true)
      isPlaces = args.getBoolean(ENABLE_PLACES, true)
      isSearch = args.getBoolean(ENABLE_SEARCH, true)
      isStyles = args.getBoolean(ENABLE_STYLES, true)
      isRadius = args.getBoolean(ENABLE_RADIUS, true)
      isBack = args.getBoolean(ENABLE_BACK, true)
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
  ) = FragmentMapBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    markerRadius = prefs.radius
    markerStyle = if (!Module.isPro && markerStyle != DEF_MARKER_STYLE) {
      DEF_MARKER_STYLE
    } else {
      prefs.markerStyle
    }
    createStyleDrawable()
    isDark = themeUtil.isDark
    setOnMapClickListener { latLng ->
      hideLayers()
      if (isTouch) {
        addMarker(latLng, markerTitle, clear = true, animate = true, radius = markerRadius)
      }
    }

    binding.mapView.onCreate(savedInstanceState)
    binding.mapView.getMapAsync(mMapCallback)

    initViews()

    binding.cardSearch.setOnItemClickListener { _, _, position, _ ->
      val sel = binding.cardSearch.getAddress(position) ?: return@setOnItemClickListener
      val lat = sel.latitude
      val lon = sel.longitude
      val pos = LatLng(lat, lon)
      addMarker(pos, getFormattedAddress(sel), clear = true, animate = true, radius = markerRadius)
    }
    initPlacesViewModel()
  }

  private fun initPlacesViewModel() {
    viewModel.places.observe(viewLifecycleOwner) { places ->
      if (places != null && isPlaces) {
        showPlaces(places)
      }
    }
  }

  private fun showRadiusDialog() {
    dialogues.showRadiusBottomDialog(requireActivity(), markerRadius) {
      recreateMarker(it)
      return@showRadiusBottomDialog getString(R.string.radius_x_meters, it.toString())
    }
  }

  private fun showStyleDialog() {
    dialogues.showColorBottomDialog(requireActivity(), prefs.markerStyle, ThemeProvider.colorsForSlider(requireContext())) {
      prefs.markerStyle = it
      recreateStyle(it)
    }
  }

  fun setOnMarkerClick(onMarkerClickListener: GoogleMap.OnMarkerClickListener?) {
    this.onMarkerClickListener = onMarkerClickListener
    mMap?.setOnMarkerClickListener(onMarkerClickListener)
  }

  fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener) {
    this.onMapClickListener = onMapClickListener
    mMap?.setOnMapClickListener(onMapClickListener)
  }

  private fun getFormattedAddress(address: Address): String {
    return if (address.getAddressLine(0) != null) {
      address.getAddressLine(0)
    } else {
      AddressAutoCompleteView.formName(address)
    }
  }

  private fun initViews() {
    binding.placesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    binding.placesList.adapter = placeRecyclerAdapter
    LinearSnapHelper().attachToRecyclerView(binding.placesList)

    binding.placesListCard.visibility = View.GONE
    binding.layersContainer.visibility = View.GONE

    binding.zoomCard.setOnClickListener { zoomClick() }
    binding.layersCard.setOnClickListener { toggleLayers() }
    binding.myCard.setOnClickListener {
      hideLayers()
      hideStyles()
      tryToMoveToMyLocation()
    }
    binding.markersCard.setOnClickListener { toggleMarkers() }
    binding.radiusCard.setOnClickListener { toggleRadius() }
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

    if (!isBack) {
      binding.backCard.visibility = View.GONE
    }
    if (!isSearch) {
      binding.searchCard.visibility = View.GONE
    }
    if (!isRadius) {
      binding.radiusCard.visibility = View.GONE
    }
    if (!isStyles || !Module.isPro) {
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
    mListener?.onBackClick()
  }

  private fun tryToSetMyLocation() {
    permissionFlow.askPermissions(
      listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)
    ) { setMyLocation() }
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

  private fun showPlaces(places: List<Place>) {
    placeRecyclerAdapter.actionsListener = object : ActionsListener<Place> {
      override fun onAction(view: View, position: Int, t: Place?, actions: ListActions) {
        when (actions) {
          ListActions.OPEN, ListActions.MORE -> {
            hideLayers()
            if (t != null) {
              if (!Module.isPro) {
                addMarker(LatLng(t.latitude, t.longitude), markerTitle, true,
                  animate = true, radius = markerRadius)
              } else {
                addMarker(LatLng(t.latitude, t.longitude), markerTitle, true,
                  t.marker, true, markerRadius)
              }
            }
          }
          else -> {
          }
        }
      }
    }
    placeRecyclerAdapter.data = places
    if (places.isEmpty()) {
      binding.placesListCard.visibility = View.GONE
    } else {
      binding.placesListCard.visibility = View.VISIBLE
    }
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

  private fun toggleRadius() {
    if (isLayersVisible) {
      hideLayers()
    }
    if (isStylesVisible) {
      hideStyles()
    }
    showRadiusDialog()
  }

  private fun showStyles() {
    binding.mapStyleContainer.visibility = View.VISIBLE
  }

  private fun toggleLayers() {
    when {
      isLayersVisible -> hideLayers()
      isStylesVisible -> hideStyles()
      else -> binding.layersContainer.visibility = View.VISIBLE
    }
  }

  private fun hideLayers() {
    if (isLayersVisible) {
      binding.layersContainer.visibility = View.GONE
    }
  }

  private fun hideStyles() {
    if (isStylesVisible) {
      binding.mapStyleContainer.visibility = View.GONE
    }
  }

  private fun zoomClick() {
    isFullscreen = !isFullscreen
    if (mListener != null) {
      mListener?.onZoomClick(isFullscreen)
    }
    if (isFullscreen) {
      binding.zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_exit_24px)
    } else {
      restoreScaleButton()
    }
  }

  override fun onResume() {
    binding.mapView.onResume()
    super.onResume()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    binding.mapView.onLowMemory()
  }

  override fun onDestroy() {
    super.onDestroy()
    runCatching { binding.mapView.onDestroy() }
  }

  override fun onPause() {
    super.onPause()
    binding.mapView.onPause()
  }

  override fun onStop() {
    super.onStop()
    binding.mapView.onStop()
  }

  private fun restoreScaleButton() {
    binding.zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_24px)
  }

  companion object {

    private const val DEF_MARKER_STYLE = 5

    const val ENABLE_TOUCH = "enable_touch"
    const val ENABLE_PLACES = "enable_places"
    const val ENABLE_SEARCH = "enable_search"
    const val ENABLE_STYLES = "enable_styles"
    const val ENABLE_BACK = "enable_back"
    const val ENABLE_ZOOM = "enable_zoom"
    const val ENABLE_RADIUS = "enable_radius"
    const val MARKER_STYLE = "marker_style"
    const val THEME_MODE = "theme_mode"

    fun newInstance(isTouch: Boolean, isPlaces: Boolean,
                    isSearch: Boolean, isStyles: Boolean,
                    isBack: Boolean, isZoom: Boolean, isDark: Boolean): AdvancedMapFragment {
      val fragment = AdvancedMapFragment()
      val args = Bundle()
      args.putBoolean(ENABLE_TOUCH, isTouch)
      args.putBoolean(ENABLE_PLACES, isPlaces)
      args.putBoolean(ENABLE_SEARCH, isSearch)
      args.putBoolean(ENABLE_STYLES, isStyles)
      args.putBoolean(ENABLE_BACK, isBack)
      args.putBoolean(ENABLE_ZOOM, isZoom)
      args.putBoolean(ENABLE_RADIUS, false)
      args.putBoolean(THEME_MODE, isDark)
      fragment.arguments = args
      return fragment
    }

    fun newInstance(isPlaces: Boolean, isStyles: Boolean, isBack: Boolean,
                    isZoom: Boolean, markerStyle: Int, isDark: Boolean, isRadius: Boolean = true): AdvancedMapFragment {
      val fragment = AdvancedMapFragment()
      val args = Bundle()
      args.putBoolean(ENABLE_PLACES, isPlaces)
      args.putBoolean(ENABLE_STYLES, isStyles)
      args.putBoolean(ENABLE_BACK, isBack)
      args.putBoolean(ENABLE_ZOOM, isZoom)
      args.putBoolean(THEME_MODE, isDark)
      args.putBoolean(ENABLE_RADIUS, isRadius)
      args.putInt(MARKER_STYLE, markerStyle)
      fragment.arguments = args
      return fragment
    }
  }
}
