package com.elementary.tasks.core.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.elementary.tasks.core.utils.BitmapUtils
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.isVisible
import com.elementary.tasks.core.utils.show
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PlacesMapFragment : BaseMapFragment<FragmentPlacesMapBinding>() {

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

  private var mLocList: LocationTracker? = null

  private var mMapListener: MapListener? = null
  private var mCallback: MapCallback? = null

  private var call: Call<PlacesResponse>? = null

  private val mMapCallback = OnMapReadyCallback { googleMap ->
    mMap = googleMap
    googleMap.uiSettings.isMyLocationButtonEnabled = false
    googleMap.uiSettings.isCompassEnabled = true
    setStyle(googleMap)
    setMyLocation()
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
          Toast.makeText(context, R.string.no_places_found, Toast.LENGTH_SHORT).show()
        }
        addSelectAllItem()
        refreshAdapter()
      } else {
        Toast.makeText(context, R.string.no_places_found, Toast.LENGTH_SHORT).show()
      }
    }

    override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
      Toast.makeText(context, R.string.no_places_found, Toast.LENGTH_SHORT).show()
    }
  }

  val places: List<Place>
    get() {
      val places = ArrayList<Place>()
      if (spinnerArray.size > 0) {
        for (model in spinnerArray) {
          if (model.isSelected) {
            if (model.position != null) {
              places.add(Place(
                radius = markerRadius,
                marker = markerStyle,
                latitude = model.latitude,
                longitude = model.longitude,
                name = model.name,
                address = model.address,
                tags = model.types))
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

  private fun addMarker(pos: LatLng?, title: String?, clear: Boolean, animate: Boolean, radius: Int = markerRadius) {
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
      mMap?.addMarker(MarkerOptions()
        .position(pos)
        .title(t)
        .icon(BitmapUtils.getDescriptor(mMarkerStyle!!))
        .draggable(clear))
      val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
      val strokeWidth = 3f
      mMap?.addCircle(CircleOptions()
        .center(pos)
        .radius(markerRadius.toDouble())
        .strokeWidth(strokeWidth)
        .fillColor(ContextCompat.getColor(context!!, marker.fillColor))
        .strokeColor(ContextCompat.getColor(context!!, marker.strokeColor)))
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
      spinnerArray.add(GooglePlaceItem(getString(R.string.add_all), "", "", "", null, listOf(), false))
    }
  }

  fun selectMarkers(list: List<Place>) {
    mMap?.clear()
    toModels(list, true)
    refreshAdapter()
  }

  fun animate(latLng: LatLng?) {
    val update = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
    if (mMap != null) {
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

  override fun layoutRes(): Int = R.layout.fragment_places_map

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    markerRadius = prefs.radius
    isDark = themeUtil.isDark

    binding.mapView.onCreate(savedInstanceState)
    binding.mapView.getMapAsync(mMapCallback)

    initViews()
    createStyleDrawable()

    binding.cardSearch.setOnEditorActionListener { _, actionId, event ->
      if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_NEXT) {
        hideKeyboard()
        loadPlaces()
        return@setOnEditorActionListener true
      }
      false
    }
  }

  private fun showStyleDialog() {
    dialogues.showColorBottomDialog(activity!!, prefs.markerStyle, ThemeUtil.colorsForSlider(activity!!)) {
      prefs.markerStyle = it
      recreateStyle(it)
    }
  }

  private fun showRadiusDialog() {
    dialogues.showRadiusBottomDialog(activity!!, markerRadius) {
      recreateMarker(it)
      return@showRadiusBottomDialog getString(R.string.radius_x_meters, it.toString())
    }
  }

  private fun createStyleDrawable() {
    mMarkerStyle = DrawableHelper.withContext(context!!)
      .withDrawable(R.drawable.ic_twotone_place_24px)
      .withColor(themeUtil.getNoteLightColor(markerStyle))
      .tint()
      .get()
  }

  private fun initViews() {
    binding.placesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(binding.cardSearch.windowToken, 0)
  }

  @SuppressLint("MissingPermission")
  private fun setMyLocation() {
    if (Permissions.checkPermission(activity!!, 205, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
      mMap?.isMyLocationEnabled = true
    }
  }

  private fun loadPlaces() {
    val req = binding.cardSearch.text.toString().trim().toLowerCase()
    if (req.matches("".toRegex())) return
    cancelSearchTask()
    call = RequestBuilder.getSearch(req)
    if (mLat != 0.0 && mLng != 0.0) {
      call = RequestBuilder.getNearby(mLat, mLng, req)
    }
    call?.enqueue(mSearchCallback)
  }

  private fun cancelSearchTask() {
    if (call != null && !call!!.isExecuted) {
      call?.cancel()
    }
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
      binding.placesListCard.show()
      binding.placesList.adapter = placesAdapter
      addMarkers()
    } else {
      binding.placesListCard.hide()
    }
  }

  private fun toModels(list: List<Place>?, select: Boolean) {
    spinnerArray = ArrayList()
    if (list != null && list.isNotEmpty()) {
      for (model in list) {
        spinnerArray.add(GooglePlaceItem(model.name, model.id, "", model.address, LatLng(model.latitude,
          model.longitude), model.tags, select))
      }
    }
  }

  private fun addMarkers() {
    mMap?.clear()
    if (spinnerArray.size > 0) {
      for (model in spinnerArray) {
        addMarker(model.position, model.name, false, false, markerRadius)
      }
    }
  }

  private fun showStyles() {
    binding.mapStyleContainer.show()
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
      else -> binding.layersContainer.show()
    }
  }

  private fun hideStyles() {
    if (isStylesVisible) {
      binding.mapStyleContainer.hide()
    }
  }

  private fun hideLayers() {
    if (isLayersVisible) {
      binding.layersContainer.hide()
    }
  }

  private fun zoomClick() {
    isFullscreen = !isFullscreen
    if (mMapListener != null) {
      mMapListener?.onZoomClick(isFullscreen)
    }
    if (isFullscreen) {
      binding.zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_exit_24px)
    } else {
      restoreScaleButton()
    }
  }

  private fun restoreScaleButton() {
    binding.zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_24px)
  }

  override fun onResume() {
    binding.mapView.onResume()
    super.onResume()
    startTracking()
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

  private fun startTracking() {
    mLocList = LocationTracker(context) { lat, lng ->
      mLat = lat
      mLng = lng
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      205 -> if (Permissions.checkPermission(grantResults)) {
        setMyLocation()
      } else {
        Toast.makeText(context, R.string.cant_access_location_services, Toast.LENGTH_SHORT).show()
      }
      200 -> if (Permissions.checkPermission(grantResults)) {
        startTracking()
      } else {
        Toast.makeText(context, R.string.cant_access_location_services, Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun cancelTracking() {
    if (mLocList != null) {
      mLocList?.removeUpdates()
    }
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
