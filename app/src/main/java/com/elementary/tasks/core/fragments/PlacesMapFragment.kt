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
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.network.Api
import com.elementary.tasks.core.network.places.PlacesResponse
import com.elementary.tasks.core.utils.*
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
class PlacesMapFragment : BaseMapFragment(), View.OnClickListener {

    private var mMap: GoogleMap? = null

    private var spinnerArray: MutableList<GooglePlaceItem> = mutableListOf()

    private var isZoom = true
    private var isFullscreen = false
    private var isDark = false
    private var mRadius = -1
    private var markerStyle = -1
    private var mLat: Double = 0.toDouble()
    private var mLng: Double = 0.toDouble()

    private var mLocList: LocationTracker? = null

    private var mMapListener: MapListener? = null
    private var mCallback: MapCallback? = null

    private var call: Call<PlacesResponse>? = null

    private val mMapCallback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        mMap!!.uiSettings.isCompassEnabled = true
        setStyle(mMap!!)
        setMyLocation()
        mMap!!.setOnMapClickListener {
            hideLayers()
            hidePlaces()
            hideStyles()
        }
        if (mCallback != null) {
            mCallback!!.onMapReady()
        }
    }
    private val mSearchCallback = object : Callback<PlacesResponse> {
        override fun onResponse(call: Call<PlacesResponse>, response: Response<PlacesResponse>) {
            if (response.code() == Api.OK) {
                val places = ArrayList<GooglePlaceItem>()
                for (place in response.body()!!.results!!) {
                    places.add(PlaceParser.getDetails(place))
                }
                spinnerArray = places
                if (spinnerArray.size == 0) {
                    Toast.makeText(context, SuperUtil.getString(this@PlacesMapFragment, R.string.no_places_found), Toast.LENGTH_SHORT).show()
                }
                addSelectAllItem()
                refreshAdapter(true)
            } else {
                Toast.makeText(context, SuperUtil.getString(this@PlacesMapFragment, R.string.no_places_found), Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
            Toast.makeText(context, SuperUtil.getString(this@PlacesMapFragment, R.string.no_places_found), Toast.LENGTH_SHORT).show()
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

    private val isPlacesVisible: Boolean
        get() = placesListCard.visibility == View.VISIBLE

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
        var title = title
        if (mMap != null && pos != null) {
            if (pos.latitude == 0.0 && pos.longitude == 0.0) return
            mRadius = radius
            if (mRadius == -1) {
                mRadius = prefs.radius
            }
            if (clear) {
                mMap!!.clear()
            }
            if (title == null || title.matches("".toRegex())) {
                title = pos.toString()
            }
            mMap!!.addMarker(MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(themeUtil.getMarkerStyle(markerStyle)))
                    .draggable(clear))
            val marker = themeUtil.getMarkerRadiusStyle(markerStyle)
            val strokeWidth = 3f
            mMap!!.addCircle(CircleOptions()
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
            spinnerArray.add(GooglePlaceItem(SuperUtil.getString(this@PlacesMapFragment, R.string.add_all), "", "", "", null, listOf(), false))
        }
    }

    fun selectMarkers(list: List<Place>) {
        mMap!!.clear()
        toModels(list, true)
        refreshAdapter(false)
    }

    fun animate(latLng: LatLng?) {
        val update = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
        if (mMap != null) {
            mMap!!.animateCamera(update)
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
        cardSearch.setHint(R.string.search_place)
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
        placesList.layoutManager = LinearLayoutManager(context)

        placesListCard.visibility = View.GONE
        styleCard.visibility = View.GONE

        zoomCard.setCardBackgroundColor(themeUtil.cardStyle)
        searchCard.setCardBackgroundColor(themeUtil.cardStyle)
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
        layersContainer.setCardBackgroundColor(style)
        layersCard.setCardBackgroundColor(style)
        placesCard.setCardBackgroundColor(style)
        styleCard.setCardBackgroundColor(style)
        placesListCard.setCardBackgroundColor(style)
        markersCard.setCardBackgroundColor(style)
        backCard.setCardBackgroundColor(style)

        cardClear.setOnClickListener(this)
        mapZoom.setOnClickListener(this)
        layers.setOnClickListener(this)
        markers.setOnClickListener(this)
        placesButton.setOnClickListener(this)

        typeNormal.setOnClickListener(this)
        typeSatellite.setOnClickListener(this)
        typeHybrid.setOnClickListener(this)
        typeTerrain.setOnClickListener(this)

        backCard.visibility = View.GONE
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
        imm?.hideSoftInputFromWindow(cardSearch!!.windowToken, 0)
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
            ib.setOnClickListener(this)
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
            mMap!!.isMyLocationEnabled = true
        }
    }

    private fun loadPlaces() {
        val req = cardSearch!!.text!!.toString().trim { it <= ' ' }.toLowerCase()
        if (req.matches("".toRegex())) return
        cancelSearchTask()
        call = RequestBuilder.getSearch(req)
        if (mLat != 0.0 && mLng != 0.0) {
            call = RequestBuilder.getNearby(mLat, mLng, req)
        }
        call!!.enqueue(mSearchCallback)
    }

    private fun cancelSearchTask() {
        if (call != null && !call!!.isExecuted) {
            call!!.cancel()
        }
    }

    private fun refreshAdapter(show: Boolean) {
        val placesAdapter = GooglePlacesAdapter()
        placesAdapter.setPlaces(spinnerArray)
        placesAdapter.setEventListener(object : SimpleListener {
            override fun onItemClicked(position: Int, view: View) {
                hideLayers()
                hidePlaces()
                animate(spinnerArray[position].position)
            }

            override fun onItemLongClicked(position: Int, view: View) {

            }
        })
        if (spinnerArray.size > 0) {
            emptyItem.visibility = View.GONE
            placesList.visibility = View.VISIBLE
            placesList.adapter = placesAdapter
            addMarkers()
            if (!isPlacesVisible && show) ViewUtils.slideInUp(context!!, placesListCard!!)
        } else {
            placesList.visibility = View.GONE
            emptyItem.visibility = View.VISIBLE
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
        mMap!!.clear()
        if (spinnerArray.size > 0) {
            for (model in spinnerArray) {
                addMarker(model.position, model.name, false, false, mRadius)
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
            ViewUtils.showOver(layersContainer)
        }
    }

    private fun hideLayers() {
        if (isLayersVisible) {
            ViewUtils.hideOver(layersContainer)
        }
    }

    private fun zoomClick() {
        isFullscreen = !isFullscreen
        if (mMapListener != null) {
            mMapListener!!.onZoomClick(isFullscreen)
        }
        if (isFullscreen) {
            if (isDark)
                mapZoom.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
            else
                mapZoom.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
        } else {
            if (isDark)
                mapZoom.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
            else
                mapZoom.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
        }
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
        startTracking()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        cancelTracking()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        cancelTracking()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        cancelTracking()
    }

    private fun startTracking() {
        mLocList = LocationTracker(context) { lat, lng ->
            mLat = lat
            mLng = lng
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

    override fun onClick(v: View) {
        val id = v.id
        if (id >= ThemeUtil.NUM_OF_MARKERS && id < ThemeUtil.NUM_OF_MARKERS * 2) {
            recreateStyle(v.id - ThemeUtil.NUM_OF_MARKERS)
            hideStyles()
        }
        when (id) {
            R.id.cardClear -> loadPlaces()
            R.id.mapZoom -> zoomClick()
            R.id.layers -> toggleLayers()
            R.id.typeNormal -> setMapType(mMap!!, GoogleMap.MAP_TYPE_NORMAL) { this.hideLayers() }
            R.id.typeHybrid -> setMapType(mMap!!, GoogleMap.MAP_TYPE_HYBRID) { this.hideLayers() }
            R.id.typeSatellite -> setMapType(mMap!!, GoogleMap.MAP_TYPE_SATELLITE) { this.hideLayers() }
            R.id.typeTerrain -> setMapType(mMap!!, GoogleMap.MAP_TYPE_TERRAIN) { this.hideLayers() }
            R.id.places -> togglePlaces()
            R.id.markers -> toggleMarkers()
        }
    }

    private fun cancelTracking() {
        if (mLocList != null) {
            mLocList!!.removeUpdates()
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
