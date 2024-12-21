package com.elementary.tasks.simplemap

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Criteria
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.elementary.tasks.config.RadiusConfig
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.fragments.BaseMapFragment
import com.elementary.tasks.core.os.Permissions
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.android.colorOf
import com.github.naz013.feature.common.android.readSerializable
import com.github.naz013.feature.common.android.toast
import com.elementary.tasks.core.utils.GeocoderTask
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.github.naz013.feature.common.android.gone
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.github.naz013.feature.common.android.visibleGone
import com.elementary.tasks.core.views.AddressAutoCompleteView
import com.elementary.tasks.databinding.FragmentSimpleMapBinding
import com.elementary.tasks.databinding.ViewMapCustomButtonBinding
import com.elementary.tasks.places.list.PlacesViewModel
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.annotations.SerializedName
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

class SimpleMapFragment : BaseMapFragment<FragmentSimpleMapBinding>() {

  var mapCallback: MapCallback? = null
  var customButtonCallback: CustomButtonCallback? = null
  var radiusChangeListener: RadiusChangeListener? = null
  var isMapReady: Boolean = false
    private set

  private val viewModel by viewModel<PlacesViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val geocoderTask by inject<GeocoderTask>()

  private var internalMap: GoogleMap? = null
  private var delayedMarkerAction: DelayedMarkerAction? = null

  private var mapParams: MapParams = MapParams()
  private var markerState: MarkerState? = null
  private var customButtonsMap = mutableMapOf<Int, CustomButton>()

  private var onMapClickListener: GoogleMap.OnMapClickListener? = null
  private var onMarkerClickListener: GoogleMap.OnMarkerClickListener? = null

  private lateinit var markerStyleController: MarkerStyleController
  private lateinit var markerRadiusController: MarkerRadiusController
  private lateinit var mapLayerController: MapLayerController
  private lateinit var recentPlacesController: RecentPlacesController

  private val mapReadyCallback = OnMapReadyCallback { googleMap ->
    internalMap = googleMap
    isMapReady = true

    googleMap.uiSettings.isMyLocationButtonEnabled = false
    googleMap.uiSettings.isCompassEnabled = false
    googleMap.uiSettings.isMapToolbarEnabled = false

    setStyle(googleMap)
    tryToSetMyLocation()
    moveToMyLocation()

    if (mapParams.isTouch) {
      googleMap.setOnMapClickListener {
        if (!hideAllLayers()) {
          addMarker(it)
          onMapClickListener?.onMapClick(it)
        }
      }
    }
    setOnMarkerClick(onMarkerClickListener)

    delayedMarkerAction?.run {
      addMarker(
        markerState = this.markerState,
        clearMap = this.clearMap,
        animate = this.animate
      )
      delayedMarkerAction = null
    }
    mapCallback?.onMapReady()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.run {
      mapParams = obtainParams(this)
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSimpleMapBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initMarkerStyleController(view)
    initMarkerRadiusController(view)
    initMapLayerController(view)
    initViews(view)

    binding.mapView.onCreate(savedInstanceState)
    binding.mapView.getMapAsync(mapReadyCallback)

    binding.cardSearch.setOnItemClickListener { _, _, position, _ ->
      val sel = binding.cardSearch.getAddress(position) ?: return@setOnItemClickListener
      addMarker(
        latLng = LatLng(sel.latitude, sel.longitude),
        title = getFormattedAddress(sel),
        clear = true,
        animate = true
      )
    }
    initPlacesViewModel()
  }

  fun addMarker(
    latLng: LatLng,
    title: String? = null,
    clear: Boolean = true,
    animate: Boolean = true
  ) {
    addMarker(
      markerState = (markerState ?: createMarkerState(latLng = latLng)).copy(
        title = title ?: geocodeAddress(latLng),
        latLng = latLng
      ),
      clearMap = clear,
      animate = animate
    )
  }

  fun addMarker(
    latLng: com.github.naz013.domain.place.LatLng,
    title: String? = null,
    clear: Boolean = true,
    animate: Boolean = true
  ) {
    val old = LatLng(latLng.latitude, latLng.longitude)
    addMarker(
      markerState = (markerState ?: createMarkerState(latLng = old)).copy(
        title = title ?: geocodeAddress(old),
        latLng = old
      ),
      clearMap = clear,
      animate = animate
    )
  }

  fun addMarker(
    latLng: LatLng,
    title: String?,
    markerStyle: Int,
    radius: Int,
    clear: Boolean,
    animate: Boolean
  ) {
    if (!::markerStyleController.isInitialized) {
      return
    }
    if (!::markerRadiusController.isInitialized) {
      return
    }
    addMarker(
      markerState = (markerState ?: createMarkerState(latLng = latLng)).copy(
        title = title ?: geocodeAddress(latLng),
        style = markerStyle,
        radius = radius,
        latLng = latLng
      ),
      clearMap = clear,
      animate = animate
    )
  }

  fun addMarker(
    latLng: com.github.naz013.domain.place.LatLng,
    title: String?,
    markerStyle: Int,
    radius: Int,
    clear: Boolean,
    animate: Boolean
  ) {
    if (!::markerStyleController.isInitialized) {
      return
    }
    if (!::markerRadiusController.isInitialized) {
      return
    }
    val old = LatLng(latLng.latitude, latLng.longitude)
    addMarker(
      markerState = (markerState ?: createMarkerState(latLng = old)).copy(
        title = title ?: geocodeAddress(old),
        style = markerStyle,
        radius = radius,
        latLng = old
      ),
      clearMap = clear,
      animate = animate
    )
  }

  fun moveCamera(
    pos: LatLng,
    paddingLeft: Int = 0,
    paddingTop: Int = 0,
    paddingRight: Int = 0,
    paddingBottom: Int = 0
  ) {
    internalMap?.run {
      animate(pos)
      setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }
  }

  fun onBackPressed(): Boolean {
    return when {
      hideAllLayers() -> false
      else -> true
    }
  }

  fun setOnMarkerClick(onMarkerClickListener: GoogleMap.OnMarkerClickListener?) {
    this.onMarkerClickListener = onMarkerClickListener
    internalMap?.setOnMarkerClickListener(onMarkerClickListener)
  }

  fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener) {
    this.onMapClickListener = onMapClickListener
    internalMap?.setOnMapClickListener(onMapClickListener)
  }

  fun changeRadius(radius: Int) {
    if (mapParams.isRadius && isMapReady && ::markerRadiusController.isInitialized) {
      markerRadiusController.setRadius(radius)
    }
    onMarkerStateChanged { copy(radius = radius) }
  }

  fun changeCustomButton(customButton: MapCustomButton) {
    if (customButtonsMap.containsKey(customButton.id)) {
      customButtonsMap[customButton.id]?.run {
        view.customButtonIconView.setImageResource(customButton.icon)
      }
      customButtonsMap[customButton.id]?.customButton = customButton
    }
  }

  private fun geocodeAddress(latLng: LatLng): String {
    return geocoderTask.getAddressForLocation(latLng) ?: latLng.toString()
  }

  private fun animate(latLng: LatLng) {
    val update = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
    internalMap?.animateCamera(update)
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
    internalMap?.run {
      val locationManager = systemServiceProvider.provideLocationManager()
      val criteria = Criteria()
      var location: Location? = null
      try {
        location = locationManager?.getLastKnownLocation(
          locationManager.getBestProvider(criteria, false)
            ?: ""
        )
      } catch (e: Throwable) {
        Logger.d("moveToMyLocation: ${e.message}")
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

  private fun getFormattedAddress(address: Address): String {
    return if (address.getAddressLine(0) != null) {
      address.getAddressLine(0)
    } else {
      AddressAutoCompleteView.formName(address)
    }
  }

  private fun initViews(view: View) {
    recentPlacesController = RecentPlacesController(
      rootView = view,
      placesAllowed = mapParams.isPlaces,
      listener = object : RecentPlacesController.OnPlaceSelectedListener {
        override fun onPlaceSelected(place: UiPlaceList) {
          if (!Module.isPro) {
            addMarker(
              latLng = place.latLng,
              title = place.name,
              clear = true,
              animate = true
            )
          } else {
            addMarker(
              latLng = place.latLng,
              title = place.name,
              markerStyle = place.markerStyle,
              radius = markerRadiusController.selectedRadius,
              clear = true,
              animate = true
            )
          }
        }

        override fun onPlaceButtonClicked() {
          mapLayerController.onOutsideClick()
          markerRadiusController.onOutsideClick()
          markerStyleController.onOutsideClick()
        }
      }
    )

    binding.placesListCard.gone()
    binding.placesButtonCard.gone()

    binding.myCard.setOnClickListener {
      hideAllLayers()
      tryToMoveToMyLocation()
    }

    binding.searchCard.visibleGone(mapParams.isSearch)
    binding.radiusCard.visibleGone(mapParams.isRadius)
    binding.markersCard.visibleGone(mapParams.isStyles && Module.isPro)
    binding.layersCard.visibleGone(mapParams.isLayers)

    binding.customButtonsContainer.removeAllViewsInLayout()

    if (mapParams.customButtons.isNotEmpty()) {
      val layoutParams = LinearLayout.LayoutParams(
        resources.getDimensionPixelSize(R.dimen.map_button_size),
        resources.getDimensionPixelSize(R.dimen.map_button_size)
      )
      val buttonMargin = resources.getDimensionPixelSize(R.dimen.map_button_margin)
      layoutParams.marginStart = buttonMargin
      layoutParams.marginEnd = buttonMargin
      layoutParams.topMargin = buttonMargin

      mapParams.customButtons.forEach {
        val button = createCustomButton(binding.customButtonsContainer, it)
        customButtonsMap[it.id] = CustomButton(button, it)
        binding.customButtonsContainer.addView(button.root, layoutParams)
      }
    }
  }

  private fun createCustomButton(
    parent: LinearLayout,
    customButton: MapCustomButton
  ): ViewMapCustomButtonBinding {
    val buttonBinding = ViewMapCustomButtonBinding.inflate(layoutInflater, parent, false)
    buttonBinding.customButtonIconView.setImageResource(customButton.icon)
    buttonBinding.root.setOnClickListener {
      customButtonCallback?.onButtonClicked(customButton.id)
    }
    buttonBinding.root.setOnLongClickListener {
      customButtonsMap[customButton.id]?.customButton?.contentDescription?.also {
        toast(it, Toast.LENGTH_SHORT)
      }
      true
    }
    return buttonBinding
  }

  private fun initPlacesViewModel() {
    viewModel.places.nonNullObserve(viewLifecycleOwner) { places ->
      if (mapParams.isPlaces) {
        recentPlacesController.onPlacesLoaded(places)
      }
    }
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
      internalMap?.isMyLocationEnabled = true
    }
  }

  private fun hideAllLayers(): Boolean {
    return when {
      mapLayerController.isLayerVisible() -> {
        mapLayerController.onOutsideClick()
        true
      }

      markerStyleController.isLayerVisible() -> {
        markerStyleController.onOutsideClick()
        true
      }

      markerRadiusController.isLayerVisible() -> {
        markerRadiusController.onOutsideClick()
        true
      }

      recentPlacesController.isLayerVisible() -> {
        recentPlacesController.onOutsideClick()
        true
      }

      else -> false
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

  private fun addMarker(
    markerState: MarkerState,
    clearMap: Boolean = true,
    animate: Boolean = true,
    afterRecreate: Boolean = false
  ) {
    if (!isMapReady) {
      delayedMarkerAction = DelayedMarkerAction(
        markerState = markerState,
        clearMap = clearMap,
        animate = animate
      )
      return
    }
    val map = internalMap ?: return

    this.markerState = markerState
    mapCallback?.onLocationSelected(markerState)

    if (!afterRecreate && mapParams.isStyles && ::markerStyleController.isInitialized) {
      markerStyleController.setStyle(markerState.style)
    }
    if (!afterRecreate && mapParams.isRadius && ::markerRadiusController.isInitialized) {
      markerRadiusController.setRadius(markerState.radius)
    }

    if (clearMap) {
      map.clear()
    }

    val markerOptions = MarkerOptions().apply {
      position(markerState.latLng)
      title(markerState.title)
      draggable(false)
    }
    markerState.drawable?.let { BitmapUtils.getDescriptor(it) }
      ?.also { markerOptions.icon(it) }
      ?: run {
        createDrawable(markerState.style)
      }.let { BitmapUtils.getDescriptor(it) }
        .also { markerOptions.icon(it) }

    map.addMarker(markerOptions)

    val markerCircle = themeUtil.getMarkerRadiusStyle(markerState.style)
    val circleOptions = CircleOptions().apply {
      center(markerState.latLng)
      radius(markerState.radius.toDouble())
      strokeWidth(markerState.strokeWidth)
      fillColor(colorOf(markerCircle.fillColor))
      strokeColor(colorOf(markerCircle.strokeColor))
    }
    map.addCircle(circleOptions)

    if (animate) {
      animate(markerState.latLng)
    }
  }

  private fun recreateMarker(markerState: MarkerState) {
    addMarker(
      markerState = markerState,
      clearMap = true,
      animate = true,
      afterRecreate = true
    )
  }

  private fun obtainParams(bundle: Bundle): MapParams {
    return bundle.readSerializable(KEY_PARAMS, MapParams::class.java) ?: MapParams()
  }

  private fun initMarkerStyleController(view: View) {
    val initStyle = mapParams.markerStyle.takeIf { it != -1 } ?: prefs.markerStyle
    markerStyleController = MarkerStyleController(
      rootView = view,
      startColor = initStyle,
      colors = ThemeProvider.colorsForSlider(requireContext()),
      selectorColor = themeUtil.pickColorRes(R.color.pureBlack, R.color.pureWhite),
      listener = object : MarkerStyleController.OnStyleSelectedListener {
        override fun onStyleSelected(style: Int) {
          if (mapParams.rememberMarkerStyle) {
            prefs.markerStyle = style
          }
          onMarkerStateChanged {
            copy(
              style = style,
              drawable = createDrawable(style)
            )
          }
        }

        override fun onStyleButtonClicked() {
          mapLayerController.onOutsideClick()
          markerRadiusController.onOutsideClick()
          recentPlacesController.onOutsideClick()
        }
      }
    )
  }

  private fun initMarkerRadiusController(view: View) {
    val initRadius = mapParams.radiusParams.radius.takeIf { it != -1 } ?: prefs.radius
    markerRadiusController = MarkerRadiusController(
      rootView = view,
      currentRadius = initRadius,
      formatter = DefaultRadiusFormatter(requireContext(), prefs.useMetric),
      listener = object : MarkerRadiusController.OnRadiusChangedListener {
        override fun onChanged(radius: Int) {
          if (mapParams.rememberMarkerRadius) {
            prefs.radius = radius
          }
          radiusChangeListener?.onRadiusChanged(radius)
          onMarkerStateChanged { copy(radius = radius) }
        }

        override fun onRadiusButtonClicked() {
          markerStyleController.onOutsideClick()
          mapLayerController.onOutsideClick()
          recentPlacesController.onOutsideClick()
        }
      }
    )
  }

  private fun initMapLayerController(view: View) {
    mapLayerController = MapLayerController(
      rootView = view,
      listener = object : MapLayerController.OnLayerStyleListener {
        override fun onLayerChanged(type: Int) {
          if (mapParams.rememberMapStyle) {
            prefs.mapType = type
          }
          mapParams = mapParams.copy(
            mapStyleParams = mapParams.mapStyleParams.copy(
              mapType = type
            )
          )
          internalMap?.run {
            setStyle(
              map = this,
              mapType = type,
              mapStyle = mapParams.mapStyleParams.mapStyle
            )
          }
        }

        override fun onStyleChanged(style: Int) {
          if (mapParams.rememberMapStyle) {
            prefs.mapStyle = style
          }
          mapParams = mapParams.copy(
            mapStyleParams = mapParams.mapStyleParams.copy(
              mapStyle = style
            )
          )
          internalMap?.run {
            setStyle(
              map = this,
              mapType = mapParams.mapStyleParams.mapType,
              mapStyle = style
            )
          }
        }

        override fun onLayerButtonClicked() {
          markerStyleController.onOutsideClick()
          markerRadiusController.onOutsideClick()
          recentPlacesController.onOutsideClick()
        }
      }
    )
  }

  private fun onMarkerStateChanged(f: MarkerState.() -> MarkerState) {
    markerState?.let { f(it) }
      ?.also { recreateMarker(it) }
  }

  private fun createMarkerState(latLng: LatLng): MarkerState {
    return markerState?.copy(
      latLng = latLng
    ) ?: MarkerState(
      latLng = latLng,
      style = markerStyleController.selectedStyle,
      radius = markerRadiusController.selectedRadius,
      title = latLng.toString(),
      drawable = createDrawable(markerStyleController.selectedStyle)
    )
  }

  private fun createDrawable(markerStyle: Int): Drawable {
    return DrawableHelper.withContext(requireContext())
      .withDrawable(R.drawable.ic_fluent_place)
      .withColor(themeUtil.getMarkerLightColor(markerStyle))
      .tint()
      .get()
  }

  data class MapParams(
    @SerializedName("isTouch")
    val isTouch: Boolean = true,
    @SerializedName("isStyles")
    val isStyles: Boolean = true,
    @SerializedName("isPlaces")
    val isPlaces: Boolean = true,
    @SerializedName("isSearch")
    val isSearch: Boolean = true,
    @SerializedName("isRadius")
    val isRadius: Boolean = true,
    @SerializedName("isLayers")
    val isLayers: Boolean = true,
    @SerializedName("rememberMapStyle")
    val rememberMapStyle: Boolean = true,
    @SerializedName("rememberMarkerRadius")
    val rememberMarkerRadius: Boolean = true,
    @SerializedName("rememberMarkerStyle")
    val rememberMarkerStyle: Boolean = true,
    @SerializedName("markerStyle")
    val markerStyle: Int = -1,
    @SerializedName("radiusParams")
    val radiusParams: RadiusParams = RadiusParams(),
    @SerializedName("mapParams")
    val mapStyleParams: MapStyleParams = MapStyleParams(),
    @SerializedName("customButtons")
    val customButtons: List<MapCustomButton> = emptyList()
  ) : Serializable

  data class RadiusParams(
    @SerializedName("min_radius")
    val minRadius: Int = RadiusConfig.MIN_RADIUS,
    @SerializedName("max_radius")
    val maxRadius: Int = RadiusConfig.MAX_RADIUS,
    @SerializedName("radius")
    val radius: Int = -1
  ) : Serializable

  data class MapStyleParams(
    @SerializedName("map_type")
    val mapType: Int = GoogleMap.MAP_TYPE_NORMAL,
    @SerializedName("map_style")
    val mapStyle: Int = 0
  ) : Serializable

  data class MapCustomButton(
    @SerializedName("icon")
    @DrawableRes
    val icon: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("content_description")
    val contentDescription: String? = null
  ) : Serializable

  data class MarkerState(
    val latLng: LatLng,
    val style: Int,
    val radius: Int,
    val title: String = "",
    val strokeWidth: Float = 3f,
    val drawable: Drawable? = null,
    val address: String = ""
  )

  private data class DelayedMarkerAction(
    val markerState: MarkerState,
    val clearMap: Boolean = true,
    val animate: Boolean = true
  )

  private data class CustomButton(
    val view: ViewMapCustomButtonBinding,
    var customButton: MapCustomButton
  )

  interface MapCallback {
    fun onMapReady()
    fun onLocationSelected(markerState: MarkerState)
  }

  abstract class DefaultMapCallback : MapCallback {
    override fun onMapReady() { }

    override fun onLocationSelected(markerState: MarkerState) { }
  }

  interface CustomButtonCallback {
    fun onButtonClicked(buttonId: Int)
  }

  interface RadiusChangeListener {
    fun onRadiusChanged(radius: Int)
  }

  companion object {

    private const val KEY_PARAMS = "key_params"

    fun newInstance(mapParams: MapParams): SimpleMapFragment {
      return SimpleMapFragment().apply {
        arguments = Bundle().apply {
          putSerializable(KEY_PARAMS, mapParams)
        }
      }
    }
  }
}
