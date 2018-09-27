package com.elementary.tasks.core.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.places.PlacesViewModel
import com.elementary.tasks.places.list.PlacesRecyclerAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*

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

    private var placeRecyclerAdapter = PlacesRecyclerAdapter()

    private var isTouch = true
    private var isZoom = true
    private var isBack = true
    private var isStyles = true
    private var isPlaces = true
    private var isSearch = true
    var isFullscreen = false
    private var isDark = false
    private var markerTitle: String = ""
    private var markerRadius = -1
    var markerStyle = -1
        private set
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
        googleMap.setOnMapClickListener(onMapClickListener)
        setOnMarkerClick(onMarkerClickListener)
        if (lastPos != null) {
            addMarker(lastPos, lastPos!!.toString(), true, false, markerRadius)
        }
        mCallback?.onMapReady()
    }

    private val isMarkersVisible: Boolean
        get() = styleCard != null && styleCard.visibility == View.VISIBLE

    private val isPlacesVisible: Boolean
        get() = placesListCard != null && placesListCard.visibility == View.VISIBLE

    private val isLayersVisible: Boolean
        get() = layersContainer != null && layersContainer.visibility == View.VISIBLE

    fun setSearchEnabled(enabled: Boolean) {
        if (cardSearch != null) {
            if (enabled) {
                searchCard.visibility = View.VISIBLE
            } else {
                searchCard.visibility = View.INVISIBLE
            }
        }
    }

    fun setAdapter(adapter: PlacesRecyclerAdapter) {
        this.placeRecyclerAdapter = adapter
    }

    fun setListener(listener: MapListener) {
        this.mListener = listener
    }

    fun setCallback(callback: MapCallback) {
        this.mCallback = callback
    }

    fun setMarkerRadius(markerRadius: Int) {
        this.markerRadius = markerRadius
    }

    fun addMarker(pos: LatLng?, title: String?, clear: Boolean, animate: Boolean, radius: Int) {
        var title = title
        if (mMap != null && pos != null) {
            markerRadius = radius
            if (markerRadius == -1)
                markerRadius = prefs.radius
            if (clear) mMap?.clear()
            if (title == null || title.matches("".toRegex())) title = pos.toString()
            if (!Module.isPro) markerStyle = 5
            lastPos = pos
            mListener?.placeChanged(pos, title)
            mMap?.addMarker(MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(themeUtil.getMarkerStyle(markerStyle)))
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
        var title = title
        var markerStyle = markerStyle
        if (mMap != null) {
            markerRadius = radius
            if (markerRadius == -1) {
                markerRadius = prefs.radius
            }
            if (!Module.isPro) markerStyle = 5
            this.markerStyle = markerStyle
            if (clear) mMap?.clear()
            if (title == null || title.matches("".toRegex())) title = pos.toString()
            lastPos = pos
            mListener?.placeChanged(pos, title)
            mMap?.addMarker(MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(themeUtil.getMarkerStyle(markerStyle)))
                    .draggable(clear))
            val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
            mMap?.addCircle(CircleOptions()
                    .center(pos)
                    .radius(markerRadius.toDouble())
                    .strokeWidth(strokeWidth)
                    .fillColor(themeUtil.getColor(marker.fillColor))
                    .strokeColor(themeUtil.getColor(marker.strokeColor)))
            if (animate) animate(pos)
            return true
        } else {
            LogUtil.d(TAG, "Map is not initialized!")
            return false
        }
    }

    fun recreateMarker(radius: Int) {
        markerRadius = radius
        if (markerRadius == -1)
            markerRadius = prefs.radius
        if (mMap != null && lastPos != null) {
            mMap?.clear()
            if (markerTitle == "" || markerTitle.matches("".toRegex()))
                markerTitle = lastPos!!.toString()
            mListener?.placeChanged(lastPos!!, markerTitle)
            if (!Module.isPro) markerStyle = 5
            mMap?.addMarker(MarkerOptions()
                    .position(lastPos!!)
                    .title(markerTitle)
                    .icon(getDescriptor(themeUtil.getMarkerStyle(markerStyle)))
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
        if (mMap != null && lastPos != null) {
            mMap?.clear()
            if (markerTitle == "" || markerTitle.matches("".toRegex()))
                markerTitle = lastPos!!.toString()
            mListener?.placeChanged(lastPos!!, markerTitle)
            if (!Module.isPro) markerStyle = 5
            mMap?.addMarker(MarkerOptions()
                    .position(lastPos!!)
                    .title(markerTitle)
                    .icon(getDescriptor(themeUtil.getMarkerStyle(markerStyle)))
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

    private fun moveToMyLocation() {
        if (!Permissions.checkPermission(context!!, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(activity!!, REQ_LOC, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)
            return
        }
        if (mMap != null) {
            val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            val criteria = Criteria()
            var location: Location? = null
            try {
                location = locationManager?.getLastKnownLocation(locationManager.getBestProvider(criteria, false))
            } catch (e: IllegalArgumentException) {
                LogUtil.e(TAG, "moveToMyLocation: ", e)
            }

            if (location != null) {
                val pos = LatLng(location.latitude, location.longitude)
                animate(pos)
            } else {
                try {
                    location = mMap!!.myLocation
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
            isMarkersVisible -> {
                hideStyles()
                false
            }
            isPlacesVisible -> {
                hidePlaces()
                false
            }
            else -> true
        }
    }

    fun showShowcase() {
        if (context == null) {
            return
        }
        if (!prefs.isShowcase(SHOWCASE) && isBack) {
            prefs.setShowcase(SHOWCASE, true)
        }
    }

    private fun initArgs() {
        val args = arguments
        if (args != null) {
            isTouch = args.getBoolean(ENABLE_TOUCH, true)
            isPlaces = args.getBoolean(ENABLE_PLACES, true)
            isSearch = args.getBoolean(ENABLE_SEARCH, true)
            isStyles = args.getBoolean(ENABLE_STYLES, true)
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
        if (!Module.isPro) {
            markerStyle = prefs.markerStyle
        }
        isDark = themeUtil.isDark
        setOnMapClickListener(GoogleMap.OnMapClickListener { latLng ->
            hideLayers()
            hidePlaces()
            hideStyles()
            if (isTouch) {
                addMarker(latLng, markerTitle, true, true, markerRadius)
            }
        })

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(mMapCallback)

        initViews()

        cardSearch.setOnItemClickListener { _, _, position, _ ->
            val sel = cardSearch!!.getAddress(position)
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
        viewModel.places.observe(this, Observer{ places ->
            if (places != null && isPlaces) {
                showPlaces(places)
            }
        })
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
        placesList.layoutManager = LinearLayoutManager(context)
        placesList.adapter = placeRecyclerAdapter

        placesListCard.visibility = View.GONE
        styleCard.visibility = View.GONE
        zoomCard.setCardBackgroundColor(themeUtil.cardStyle)
        searchCard.setCardBackgroundColor(themeUtil.cardStyle)
        myCard.setCardBackgroundColor(themeUtil.cardStyle)
        layersCard.setCardBackgroundColor(themeUtil.cardStyle)
        placesCard.setCardBackgroundColor(themeUtil.cardStyle)
        styleCard.setCardBackgroundColor(themeUtil.cardStyle)
        placesListCard.setCardBackgroundColor(themeUtil.cardStyle)
        markersCard.setCardBackgroundColor(themeUtil.cardStyle)
        backCard.setCardBackgroundColor(themeUtil.cardStyle)

        layersContainer.visibility = View.GONE
        layersContainer.setCardBackgroundColor(themeUtil.cardStyle)

        if (Module.isLollipop) {
            zoomCard.cardElevation = Configs.CARD_ELEVATION
            searchCard.cardElevation = Configs.CARD_ELEVATION
            myCard.cardElevation = Configs.CARD_ELEVATION
            layersContainer.cardElevation = Configs.CARD_ELEVATION
            layersCard.cardElevation = Configs.CARD_ELEVATION
            placesCard.cardElevation = Configs.CARD_ELEVATION
            styleCard.cardElevation = Configs.CARD_ELEVATION
            placesListCard.cardElevation = Configs.CARD_ELEVATION
            markersCard.cardElevation = Configs.CARD_ELEVATION
            backCard.cardElevation = Configs.CARD_ELEVATION
        }

        val style = themeUtil.cardStyle
        zoomCard.setCardBackgroundColor(style)
        searchCard.setCardBackgroundColor(style)
        myCard.setCardBackgroundColor(style)
        layersContainer.setCardBackgroundColor(style)
        layersCard.setCardBackgroundColor(style)
        placesCard.setCardBackgroundColor(style)
        styleCard.setCardBackgroundColor(style)
        placesListCard.setCardBackgroundColor(style)
        markersCard.setCardBackgroundColor(style)
        backCard.setCardBackgroundColor(style)

        cardClear.setOnClickListener{ cardSearch?.setText("") }
        mapZoom.setOnClickListener{ zoomClick() }
        layers.setOnClickListener{ toggleLayers() }
        myLocation.setOnClickListener{
            hideLayers()
            moveToMyLocation()
        }
        markers.setOnClickListener{ toggleMarkers() }
        places.setOnClickListener{ togglePlaces() }
        backButton.setOnClickListener{
            restoreScaleButton()
            mListener?.onBackClick()
        }

        typeNormal.setOnClickListener{
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_NORMAL) { this.hideLayers() }
        }
        typeSatellite.setOnClickListener{
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_SATELLITE) { this.hideLayers() }
        }
        typeHybrid.setOnClickListener{
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_HYBRID) { this.hideLayers() }
        }
        typeTerrain.setOnClickListener{
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_TERRAIN) { this.hideLayers() }
        }

        if (!isPlaces) {
            placesCard.visibility = View.GONE
        }
        if (!isBack) {
            backCard.visibility = View.GONE
        }
        if (!isSearch) {
            searchCard.visibility = View.GONE
        }
        if (!isStyles || !Module.isPro) {
            markersCard.visibility = View.GONE
        }
        if (!isZoom) {
            zoomCard.visibility = View.GONE
        }
        loadMarkers()
    }

    private fun loadMarkers() {
        groupOne.removeAllViewsInLayout()
        groupTwo.removeAllViewsInLayout()
        groupThree.removeAllViewsInLayout()
        for (i in 0 until ThemeUtil.NUM_OF_MARKERS) {
            val ib = ImageButton(context)
            ib.setBackgroundResource(android.R.color.transparent)
            ib.setImageResource(themeUtil.getMarkerStyle(i))
            ib.id = i + ThemeUtil.NUM_OF_MARKERS
            ib.setOnClickListener{
                recreateStyle(ib.id - ThemeUtil.NUM_OF_MARKERS)
                hideStyles()
            }
            val params = LinearLayout.LayoutParams(
                    MeasureUtils.dp2px(context!!, 35),
                    MeasureUtils.dp2px(context!!, 35))
            val px = MeasureUtils.dp2px(context!!, 2)
            params.setMargins(px, px, px, px)
            ib.layoutParams = params
            when {
                i < 5 -> groupOne.addView(ib)
                i < 10 -> groupTwo.addView(ib)
                else -> groupThree.addView(ib)
            }
        }
    }

    private fun setMyLocation() {
        if (!Permissions.checkPermission(context!!, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(activity!!, 205, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)
        } else {
            mMap?.isMyLocationEnabled = true
        }
    }

    private fun showPlaces(places: List<Place>) {
        placeRecyclerAdapter.actionsListener = object : ActionsListener<Place> {
            override fun onAction(view: View, position: Int, t: Place?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN, ListActions.MORE -> {
                        hideLayers()
                        hidePlaces()
                        if (t != null) {
                            addMarker(LatLng(t.latitude, t.longitude), markerTitle, true, true, markerRadius)
                        }
                    }
                }
            }
        }
        placeRecyclerAdapter.data = places
        if (places.isEmpty()) {
            placesCard.visibility = View.GONE
            placesList.visibility = View.GONE
            emptyItem?.visibility = View.VISIBLE
        } else {
            emptyItem?.visibility = View.GONE
            placesCard.visibility = View.VISIBLE
            placesList.visibility = View.VISIBLE
            addMarkers(places)
        }
    }

    private fun addMarkers(list: List<Place>) {
        if (list.isNotEmpty()) {
            for (model in list) {
                addMarker(LatLng(model.latitude, model.longitude), model.name, false,
                        model.marker, false, model.radius)
            }
        }
    }

    private fun toggleMarkers() {
        if (isLayersVisible) {
            hideLayers()
        }
        if (isPlacesVisible) {
            hidePlaces()
        }
        if (isMarkersVisible) {
            hideStyles()
        } else {
            ViewUtils.slideInUp(context!!, styleCard!!)
        }
    }

    private fun hideStyles() {
        if (isMarkersVisible) {
            ViewUtils.slideOutDown(context!!, styleCard!!)
        }
    }

    private fun togglePlaces() {
        if (isMarkersVisible) {
            hideStyles()
        }
        if (isLayersVisible) {
            hideLayers()
        }
        if (isPlacesVisible) {
            hidePlaces()
        } else {
            ViewUtils.slideInUp(context!!, placesListCard!!)
        }
    }

    private fun hidePlaces() {
        if (isPlacesVisible) {
            ViewUtils.slideOutDown(context!!, placesListCard!!)
        }
    }

    private fun toggleLayers() {
        if (isMarkersVisible) {
            hideStyles()
        }
        if (isPlacesVisible) {
            hidePlaces()
        }
        if (isLayersVisible) {
            hideLayers()
        } else {
            ViewUtils.showOver(layersContainer!!)
        }
    }

    private fun hideLayers() {
        if (isLayersVisible) {
            ViewUtils.hideOver(layersContainer!!)
        }
    }

    private fun zoomClick() {
        isFullscreen = !isFullscreen
        if (mListener != null) {
            mListener?.onZoomClick(isFullscreen)
        }
        if (isFullscreen) {
            if (isDark) mapZoom.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
            else mapZoom.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
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
        if (grantResults.isEmpty()) return
        when (requestCode) {
            REQ_LOC -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveToMyLocation()
            }
            205 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMyLocation()
            } else {
                Toast.makeText(context, R.string.cant_access_location_services, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restoreScaleButton() {
        if (isDark) mapZoom.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
        else mapZoom.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
    }

    companion object {

        private const val TAG = "AdvancedMapFragment"
        private const val SHOWCASE = "map_showcase"
        private const val REQ_LOC = 1245

        const val ENABLE_TOUCH = "enable_touch"
        const val ENABLE_PLACES = "enable_places"
        const val ENABLE_SEARCH = "enable_search"
        const val ENABLE_STYLES = "enable_styles"
        const val ENABLE_BACK = "enable_back"
        const val ENABLE_ZOOM = "enable_zoom"
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
            args.putBoolean(THEME_MODE, isDark)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(isPlaces: Boolean, isStyles: Boolean, isBack: Boolean,
                        isZoom: Boolean, markerStyle: Int, isDark: Boolean): AdvancedMapFragment {
            val fragment = AdvancedMapFragment()
            val args = Bundle()
            args.putBoolean(ENABLE_PLACES, isPlaces)
            args.putBoolean(ENABLE_STYLES, isStyles)
            args.putBoolean(ENABLE_BACK, isBack)
            args.putBoolean(ENABLE_ZOOM, isZoom)
            args.putBoolean(THEME_MODE, isDark)
            args.putInt(MARKER_STYLE, markerStyle)
            fragment.arguments = args
            return fragment
        }
    }
}
