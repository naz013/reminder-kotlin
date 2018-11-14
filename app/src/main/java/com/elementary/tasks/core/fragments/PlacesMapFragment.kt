package com.elementary.tasks.core.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.network.Api
import com.elementary.tasks.core.network.places.PlacesResponse
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ThemeUtil
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
import kotlinx.android.synthetic.main.fragment_places_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

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
class PlacesMapFragment : BaseMapFragment() {

    private var mMap: GoogleMap? = null

    private var spinnerArray: MutableList<GooglePlaceItem> = mutableListOf()

    private var isZoom = true
    var isFullscreen = false
    private var isDark = false
    private var mRadius = -1
    private var markerStyle = -1
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
            if (response.code() == Api.OK) {
                val places = ArrayList<GooglePlaceItem>()
                for (place in response.body()?.results!!) {
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
                            places.add(Place(mRadius, markerStyle, model.position!!.latitude,
                                    model.position!!.longitude, model.name, model.address, model.types))
                        }
                    }
                }
            }
            return places
        }

    private val isMarkersVisible: Boolean
        get() = styleCard.visibility == View.VISIBLE

    private val isLayersVisible: Boolean
        get() = layersContainer.visibility == View.VISIBLE

    fun setListener(listener: MapListener) {
        this.mMapListener = listener
    }

    fun setCallback(callback: MapCallback) {
        this.mCallback = callback
    }

    fun setRadius(mRadius: Int) {
        this.mRadius = mRadius
    }

    fun setMarkerStyle(markerStyle: Int) {
        this.markerStyle = markerStyle
    }

    fun addMarker(pos: LatLng?, title: String?, clear: Boolean, animate: Boolean, radius: Int) {
        var t = title
        if (mMap != null && pos != null) {
            if (pos.latitude == 0.0 && pos.longitude == 0.0) return
            mRadius = radius
            if (mRadius == -1) {
                mRadius = prefs.radius
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
                    .icon(getDescriptor(themeUtil.getMarkerStyle(markerStyle)))
                    .draggable(clear))
            val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
            val strokeWidth = 3f
            mMap?.addCircle(CircleOptions()
                    .center(pos)
                    .radius(mRadius.toDouble())
                    .strokeWidth(strokeWidth)
                    .fillColor(themeUtil.getColor(marker.fillColor))
                    .strokeColor(themeUtil.getColor(marker.strokeColor)))
            if (animate) {
                animate(pos)
            }
        }
    }

    fun recreateMarker(radius: Int) {
        mRadius = radius
        if (mRadius == -1) {
            mRadius = prefs.radius
        }
        if (mMap != null) {
            addMarkers()
        }
    }

    fun recreateStyle(style: Int) {
        markerStyle = style
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
            isMarkersVisible -> {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        initArgs()
        return inflater.inflate(R.layout.fragment_places_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRadius = prefs.radius
        isDark = themeUtil.isDark

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(mMapCallback)

        initViews()
        cardSearch.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard()
                loadPlaces()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun initViews() {
        placesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(placesList)

        placesListCard.visibility = View.GONE
        styleCard.visibility = View.GONE

        layersContainer.visibility = View.GONE

        zoomCard.setOnClickListener { zoomClick() }
        layersCard.setOnClickListener { toggleLayers() }
        markersCard.setOnClickListener { toggleMarkers() }
        cardClear.setOnClickListener { loadPlaces() }
        backCard.setOnClickListener {
            restoreScaleButton()
            mMapListener?.onBackClick()
        }

        typeNormal.setOnClickListener {
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_NORMAL) { this.hideLayers() }
        }
        typeSatellite.setOnClickListener {
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_SATELLITE) { this.hideLayers() }
        }
        typeHybrid.setOnClickListener {
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_HYBRID) { this.hideLayers() }
        }
        typeTerrain.setOnClickListener {
            if (mMap != null) setMapType(mMap!!, GoogleMap.MAP_TYPE_TERRAIN) { this.hideLayers() }
        }

        if (!Module.isPro) {
            markersCard.visibility = View.GONE
        }
        if (!isZoom) {
            zoomCard.visibility = View.GONE
        }
        loadMarkers()
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(cardSearch.windowToken, 0)
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

    private fun loadPlaces() {
        val req = cardSearch.text.toString().trim { it <= ' ' }.toLowerCase()
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
            placesListCard.visibility = View.VISIBLE
            placesList.adapter = placesAdapter
            addMarkers()
        } else {
            placesListCard.visibility = View.GONE
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
                addMarker(model.position, model.name, false, false, mRadius)
            }
        }
    }

    private fun toggleMarkers() {
        if (isLayersVisible) hideLayers()
        if (isMarkersVisible) {
            hideStyles()
        } else {
            styleCard.visibility = View.VISIBLE
        }
    }

    private fun hideStyles() {
        if (isMarkersVisible) {
            styleCard.visibility = View.GONE
        }
    }

    private fun toggleLayers() {
        if (isMarkersVisible) hideStyles()
        if (isLayersVisible) {
            hideLayers()
        } else {
            layersContainer.visibility = View.VISIBLE
        }
    }

    private fun hideLayers() {
        if (isLayersVisible) {
            layersContainer.visibility = View.GONE
        }
    }

    private fun zoomClick() {
        isFullscreen = !isFullscreen
        if (mMapListener != null) {
            mMapListener?.onZoomClick(isFullscreen)
        }
        if (isFullscreen) {
            zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_exit_24px)
        } else {
            restoreScaleButton()
        }
    }

    private fun restoreScaleButton() {
        zoomIcon.setImageResource(R.drawable.ic_twotone_fullscreen_24px)
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
        startTracking()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        cancelTracking()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        cancelTracking()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
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
        if (grantResults.isEmpty()) return
        when (requestCode) {
            205 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMyLocation()
            } else {
                Toast.makeText(context, R.string.cant_access_location_services, Toast.LENGTH_SHORT).show()
            }
            200 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
