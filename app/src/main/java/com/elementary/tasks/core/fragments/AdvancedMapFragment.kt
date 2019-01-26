package com.elementary.tasks.core.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.places.PlacesViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import timber.log.Timber

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class AdvancedMapFragment : BaseMapFragment() {

    private var mMap: GoogleMap? = null

    private var placeRecyclerAdapter = RecentPlacesAdapter()

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
        setMyLocation()
        googleMap.setOnMapClickListener {
            hideLayers()
            hideStyles()
            onMapClickListener?.onMapClick(it)
        }
        setOnMarkerClick(onMarkerClickListener)
        if (lastPos != null) {
            addMarker(lastPos, lastPos.toString(), true, false, markerRadius)
        }
        mCallback?.onMapReady()
    }

    private val isLayersVisible: Boolean
        get() = layersContainer != null && layersContainer.visibility == View.VISIBLE

    private val isStylesVisible: Boolean
        get() = mapStyleContainer != null && mapStyleContainer.visibility == View.VISIBLE

    fun setSearchEnabled(enabled: Boolean) {
        if (cardSearch != null) {
            if (enabled) {
                searchCard.visibility = View.VISIBLE
            } else {
                searchCard.visibility = View.GONE
            }
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
            if (t == null || t.matches("".toRegex())) t = pos.toString()
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
                    .fillColor(themeUtil.getColor(marker.fillColor))
                    .strokeColor(themeUtil.getColor(marker.strokeColor)))
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
                    .fillColor(themeUtil.getColor(marker.fillColor))
                    .strokeColor(themeUtil.getColor(marker.strokeColor)))
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
                    .center(lastPos)
                    .radius(markerRadius.toDouble())
                    .strokeWidth(strokeWidth)
                    .fillColor(themeUtil.getColor(marker.fillColor))
                    .strokeColor(themeUtil.getColor(marker.strokeColor)))
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
                        .center(lastPos)
                        .radius(markerRadius.toDouble())
                        .strokeWidth(strokeWidth)
                        .fillColor(themeUtil.getColor(marker.fillColor))
                        .strokeColor(themeUtil.getColor(marker.strokeColor)))
            }
            animate(lastPos!!)
        }
    }

    private fun createStyleDrawable() {
        mMarkerStyle = DrawableHelper.withContext(context!!)
                .withDrawable(R.drawable.ic_twotone_place_24px)
                .withColor(themeUtil.getNoteLightColor(markerStyle))
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

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun moveToMyLocation() {
        if (!Permissions.ensurePermissions(activity!!, REQ_LOC, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            return
        }
        if (mMap != null) {
            val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            val criteria = Criteria()
            var location: Location? = null
            try {
                location = locationManager?.getLastKnownLocation(locationManager.getBestProvider(criteria, false))
            } catch (e: IllegalArgumentException) {
                Timber.d("moveToMyLocation: ${e.message}")
            }

            if (location != null) {
                val pos = LatLng(location.latitude, location.longitude)
                animate(pos)
            } else {
                try {
                    location = mMap?.myLocation
                    if (location != null) {
                        val pos = LatLng(location.latitude, location.longitude)
                        animate(pos)
                    }
                } catch (ignored: IllegalStateException) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

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
        setOnMapClickListener(GoogleMap.OnMapClickListener { latLng ->
            hideLayers()
            if (isTouch) {
                addMarker(latLng, markerTitle, true, true, markerRadius)
            }
        })

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(mMapCallback)

        initViews()

        cardSearch.setOnItemClickListener { _, _, position, _ ->
            val sel = cardSearch?.getAddress(position) ?: return@setOnItemClickListener
            val lat = sel.latitude
            val lon = sel.longitude
            val pos = LatLng(lat, lon)
            addMarker(pos, markerTitle, true, true, markerRadius)
            mListener?.placeChanged(pos, getFormattedAddress(sel))
        }
        initPlacesViewModel()
    }

    private fun initPlacesViewModel() {
        val viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
        viewModel.places.observe(this, Observer { places ->
            if (places != null && isPlaces) {
                showPlaces(places)
            }
        })
    }

    private fun showRadiusDialog() {
        dialogues.showRadiusBottomDialog(activity!!, markerRadius) {
            recreateMarker(it)
            return@showRadiusBottomDialog getString(R.string.radius_x_meters, it.toString())
        }
    }

    private fun showStyleDialog() {
        dialogues.showColorBottomDialog(activity!!, prefs.markerStyle, themeUtil.colorsForSlider()) {
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
        return String.format("%s, %s%s",
                if (address.maxAddressLineIndex > 0) address.getAddressLine(0) else "",
                if (address.maxAddressLineIndex > 1) address.getAddressLine(1) + ", " else "",
                address.countryName)
    }

    private fun initViews() {
        placesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        placesList.adapter = placeRecyclerAdapter
        LinearSnapHelper().attachToRecyclerView(placesList)

        placesListCard.visibility = View.GONE
        layersContainer.visibility = View.GONE

        zoomCard.setOnClickListener { zoomClick() }
        layersCard.setOnClickListener { toggleLayers() }
        myCard.setOnClickListener {
            hideLayers()
            hideStyles()
            moveToMyLocation()
        }
        markersCard.setOnClickListener { toggleMarkers() }
        radiusCard.setOnClickListener { toggleRadius() }
        backCard.setOnClickListener { invokeBack() }

        typeNormal.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_NORMAL) }
        typeSatellite.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_SATELLITE) }
        typeHybrid.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_HYBRID) }
        typeTerrain.setOnClickListener { typeClick(GoogleMap.MAP_TYPE_TERRAIN) }

        styleDay.setOnClickListener { styleClick(0) }
        styleRetro.setOnClickListener { styleClick(1) }
        styleSilver.setOnClickListener { styleClick(2) }
        styleNight.setOnClickListener { styleClick(3) }
        styleDark.setOnClickListener { styleClick(4) }
        styleAubergine.setOnClickListener { styleClick(5) }

        if (!isBack) {
            backCard.visibility = View.GONE
        }
        if (!isSearch) {
            searchCard.visibility = View.GONE
        }
        if (!isRadius) {
            radiusCard.visibility = View.GONE
        }
        if (!isStyles || !Module.isPro) {
            markersCard.visibility = View.GONE
        }
        if (!isZoom) {
            zoomCard.visibility = View.GONE
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

    @SuppressLint("MissingPermission")
    private fun setMyLocation() {
        val context = activity ?: return
        if (Permissions.ensurePermissions(context, 205, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
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
                                addMarker(LatLng(t.latitude, t.longitude), markerTitle, true, true, markerRadius)
                            } else {
                                addMarker(LatLng(t.latitude, t.longitude), markerTitle, true, t.marker, true, markerRadius)
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
            placesListCard.visibility = View.GONE
        } else {
            placesListCard.visibility = View.VISIBLE
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
        mapStyleContainer.visibility = View.VISIBLE
    }

    private fun toggleLayers() {
        when {
            isLayersVisible -> hideLayers()
            isStylesVisible -> hideStyles()
            else -> layersContainer.visibility = View.VISIBLE
        }
    }

    private fun hideLayers() {
        if (isLayersVisible) {
            layersContainer.visibility = View.GONE
        }
    }

    private fun hideStyles() {
        if (isStylesVisible) {
            mapStyleContainer.visibility = View.GONE
        }
    }

    private fun zoomClick() {
        isFullscreen = !isFullscreen
        if (mListener != null) {
            mListener?.onZoomClick(isFullscreen)
        }
        if (isFullscreen) {
            zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_exit_24px)
        } else {
            restoreScaleButton()
        }
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_LOC -> if (Permissions.isAllGranted(grantResults)) {
                moveToMyLocation()
            }
            205 -> if (Permissions.isAllGranted(grantResults)) {
                setMyLocation()
            } else {
                Toast.makeText(context, R.string.cant_access_location_services, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restoreScaleButton() {
        zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_24px)
    }

    companion object {

        private const val REQ_LOC = 1245
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
