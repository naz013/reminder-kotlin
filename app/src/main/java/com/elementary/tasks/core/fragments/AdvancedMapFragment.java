package com.elementary.tasks.core.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.AddressAutoCompleteView;
import com.elementary.tasks.core.views.ThemedImageButton;
import com.elementary.tasks.databinding.FragmentMapBinding;
import com.elementary.tasks.places.PlaceItem;
import com.elementary.tasks.places.PlacesRecyclerAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class AdvancedMapFragment extends BaseMapFragment implements View.OnClickListener {

    private static final String TAG = "AdvancedMapFragment";
    private static final String SHOWCASE = "map_showcase";

    private GoogleMap mMap;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private AddressAutoCompleteView cardSearch;
    private ThemedImageButton zoomOut;
    private ThemedImageButton backButton;
    private ThemedImageButton places;
    private ThemedImageButton markers;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;
    private FragmentMapBinding binding;

    private PlacesRecyclerAdapter placeRecyclerAdapter;

    private boolean isTouch = true;
    private boolean isZoom = true;
    private boolean isBack = true;
    private boolean isStyles = true;
    private boolean isPlaces = true;
    private boolean isSearch = true;
    private boolean isFullscreen = false;
    private boolean isDark = false;
    private String markerTitle;
    private int markerRadius = -1;
    private int markerStyle = -1;
    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;
    private LatLng lastPos;
    private float strokeWidth = 3f;

    private MapListener mListener;
    private MapCallback mCallback;

    public static final String ENABLE_TOUCH = "enable_touch";
    public static final String ENABLE_PLACES = "enable_places";
    public static final String ENABLE_SEARCH = "enable_search";
    public static final String ENABLE_STYLES = "enable_styles";
    public static final String ENABLE_BACK = "enable_back";
    public static final String ENABLE_ZOOM = "enable_zoom";
    public static final String MARKER_STYLE = "marker_style";
    public static final String THEME_MODE = "theme_mode";

    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnMarkerClickListener onMarkerClickListener;

    private OnMapReadyCallback mMapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setMapType(mMapType);
            setMyLocation();
            mMap.setOnMapClickListener(onMapClickListener);
            setOnMarkerClick(onMarkerClickListener);
            if (lastPos != null) {
                addMarker(lastPos, lastPos.toString(), true, false, markerRadius);
            }
            if (mCallback != null) {
                mCallback.onMapReady();
            }
        }
    };

    public static AdvancedMapFragment newInstance(boolean isTouch, boolean isPlaces,
                                                  boolean isSearch, boolean isStyles,
                                                  boolean isBack, boolean isZoom, boolean isDark) {
        AdvancedMapFragment fragment = new AdvancedMapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_TOUCH, isTouch);
        args.putBoolean(ENABLE_PLACES, isPlaces);
        args.putBoolean(ENABLE_SEARCH, isSearch);
        args.putBoolean(ENABLE_STYLES, isStyles);
        args.putBoolean(ENABLE_BACK, isBack);
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        fragment.setArguments(args);
        return fragment;
    }

    public static AdvancedMapFragment newInstance(boolean isPlaces, boolean isStyles, boolean isBack,
                                                  boolean isZoom, int markerStyle, boolean isDark) {
        AdvancedMapFragment fragment = new AdvancedMapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_PLACES, isPlaces);
        args.putBoolean(ENABLE_STYLES, isStyles);
        args.putBoolean(ENABLE_BACK, isBack);
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        args.putInt(MARKER_STYLE, markerStyle);
        fragment.setArguments(args);
        return fragment;
    }

    public AdvancedMapFragment() {

    }

    public void setSearchEnabled(boolean enabled) {
        if (cardSearch != null) {
            if (enabled) {
                binding.searchCard.setVisibility(View.VISIBLE);
            } else {
                binding.searchCard.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setAdapter(PlacesRecyclerAdapter adapter) {
        this.placeRecyclerAdapter = adapter;
    }

    public void setListener(MapListener listener) {
        this.mListener = listener;
    }

    public void setCallback(MapCallback callback) {
        this.mCallback = callback;
    }

    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    public void setMarkerRadius(int markerRadius) {
        this.markerRadius = markerRadius;
    }

    public void setMarkerStyle(int markerStyle) {
        this.markerStyle = markerStyle;
    }

    public int getMarkerStyle() {
        return markerStyle;
    }

    public void addMarker(LatLng pos, String title, boolean clear, boolean animate, int radius) {
        if (mMap != null) {
            markerRadius = radius;
            if (markerRadius == -1)
                markerRadius = getPrefs().getRadius();
            if (clear) mMap.clear();
            if (title == null || title.matches("")) title = pos.toString();
            if (!Module.isPro()) markerStyle = 5;
            lastPos = pos;
            if (mListener != null) mListener.placeChanged(pos, title);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(getThemeUtil().getMarkerStyle(markerStyle)))
                    .draggable(clear));
            ThemeUtil.Marker marker = getThemeUtil().getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(getThemeUtil().getColor(marker.getFillColor()))
                    .strokeColor(getThemeUtil().getColor(marker.getStrokeColor())));
            if (animate) animate(pos);
        }
    }

    public boolean addMarker(LatLng pos, String title, boolean clear, int markerStyle, boolean animate, int radius) {
        if (mMap != null) {
            markerRadius = radius;
            if (markerRadius == -1) {
                markerRadius = getPrefs().getRadius();
            }
            if (!Module.isPro()) markerStyle = 5;
            this.markerStyle = markerStyle;
            if (clear) mMap.clear();
            if (title == null || title.matches("")) title = pos.toString();
            lastPos = pos;
            if (mListener != null) mListener.placeChanged(pos, title);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(getThemeUtil().getMarkerStyle(markerStyle)))
                    .draggable(clear));
            ThemeUtil.Marker marker = getThemeUtil().getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(getThemeUtil().getColor(marker.getFillColor()))
                    .strokeColor(getThemeUtil().getColor(marker.getStrokeColor())));
            if (animate) animate(pos);
            return true;
        } else {
            LogUtil.d(TAG, "Map is not initialized!");
            return false;
        }
    }

    public void recreateMarker(int radius) {
        markerRadius = radius;
        if (markerRadius == -1)
            markerRadius = getPrefs().getRadius();
        if (mMap != null && lastPos != null) {
            mMap.clear();
            if (markerTitle == null || markerTitle.matches(""))
                markerTitle = lastPos.toString();
            if (mListener != null) mListener.placeChanged(lastPos, markerTitle);
            if (!Module.isPro()) markerStyle = 5;
            mMap.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(getDescriptor(getThemeUtil().getMarkerStyle(markerStyle)))
                    .draggable(true));
            ThemeUtil.Marker marker = getThemeUtil().getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(lastPos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(getThemeUtil().getColor(marker.getFillColor()))
                    .strokeColor(getThemeUtil().getColor(marker.getStrokeColor())));
            animate(lastPos);
        }
    }

    public void recreateStyle(int style) {
        markerStyle = style;
        if (mMap != null && lastPos != null) {
            mMap.clear();
            if (markerTitle == null || markerTitle.matches(""))
                markerTitle = lastPos.toString();
            if (mListener != null) mListener.placeChanged(lastPos, markerTitle);
            if (!Module.isPro()) markerStyle = 5;
            mMap.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(getDescriptor(getThemeUtil().getMarkerStyle(markerStyle)))
                    .draggable(true));
            if (markerStyle >= 0) {
                ThemeUtil.Marker marker = getThemeUtil().getMarkerRadiusStyle(markerStyle);
                if (markerRadius == -1) {
                    markerRadius = getPrefs().getRadius();
                }
                mMap.addCircle(new CircleOptions()
                        .center(lastPos)
                        .radius(markerRadius)
                        .strokeWidth(strokeWidth)
                        .fillColor(getThemeUtil().getColor(marker.getFillColor()))
                        .strokeColor(getThemeUtil().getColor(marker.getStrokeColor())));
            }
            animate(lastPos);
        }
    }

    public void moveCamera(LatLng pos, int i1, int i2, int i3, int i4) {
        if (mMap != null) {
            animate(pos);
            mMap.setPadding(i1, i2, i3, i4);
        }
    }

    public void animate(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (mMap != null) mMap.animateCamera(update);
    }

    @SuppressWarnings("MissingPermission")
    public void moveToMyLocation() {
        if (mMap != null) {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                animate(pos);
            } else {
                try {
                    location = mMap.getMyLocation();
                    if (location != null) {
                        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                        animate(pos);
                    }
                } catch (IllegalStateException ignored) {
                }
            }
        }
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
    }

    public boolean onBackPressed() {
        if (isLayersVisible()) {
            hideLayers();
            return false;
        } else if (isMarkersVisible()) {
            hideStyles();
            return false;
        } else if (isPlacesVisible()) {
            hidePlaces();
            return false;
        } else {
            return true;
        }
    }

    public void showShowcase() {
        if (getContext() == null) {
            return;
        }
        if (!getPrefs().isShowcase(SHOWCASE) && isBack) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(getThemeUtil().getColor(getThemeUtil().colorAccent()));
            config.setContentTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            config.setDismissTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getContext());
            sequence.setConfig(config);
            sequence.addSequenceItem(zoomOut,
                    getContext().getString(R.string.click_to_expand_collapse_map),
                    getContext().getString(R.string.got_it));
            sequence.addSequenceItem(backButton,
                    getContext().getString(R.string.click_when_add_place),
                    getContext().getString(R.string.got_it));
            if (Module.isPro()) {
                sequence.addSequenceItem(markers,
                        getContext().getString(R.string.select_style_for_marker),
                        getContext().getString(R.string.got_it));
            }
            sequence.addSequenceItem(places,
                    getContext().getString(R.string.select_place_from_list),
                    getContext().getString(R.string.got_it));
            sequence.start();
            getPrefs().setShowcase(SHOWCASE, true);
        }
    }

    private void initArgs() {
        Bundle args = getArguments();
        if (args != null) {
            isTouch = args.getBoolean(ENABLE_TOUCH, true);
            isPlaces = args.getBoolean(ENABLE_PLACES, true);
            isSearch = args.getBoolean(ENABLE_SEARCH, true);
            isStyles = args.getBoolean(ENABLE_STYLES, true);
            isBack = args.getBoolean(ENABLE_BACK, true);
            isZoom = args.getBoolean(ENABLE_ZOOM, true);
            isDark = args.getBoolean(THEME_MODE, false);
            markerStyle = args.getInt(MARKER_STYLE, getPrefs().getMarkerStyle());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initArgs();
        binding = FragmentMapBinding.inflate(inflater, container, false);
        markerRadius = getPrefs().getRadius();
        mMapType = getPrefs().getMapType();
        if (!Module.isPro()) {
            markerStyle = getPrefs().getMarkerStyle();
        }
        isDark = getThemeUtil().isDark();
        setOnMapClickListener(onMapClickListener = latLng -> {
            hideLayers();
            hidePlaces();
            hideStyles();
            if (isTouch) {
                addMarker(latLng, markerTitle, true, true, markerRadius);
            }
        });
        com.google.android.gms.maps.MapFragment fragment = com.google.android.gms.maps.MapFragment.newInstance();
        fragment.getMapAsync(mMapCallback);
        getFragmentManager().beginTransaction()
                .add(R.id.map, fragment)
                .commit();
        initViews();
        cardSearch = binding.cardSearch;
        cardSearch.setOnItemClickListener((parent, view1, position, id) -> {
            Address sel = cardSearch.getAddress(position);
            double lat = sel.getLatitude();
            double lon = sel.getLongitude();
            LatLng pos = new LatLng(lat, lon);
            addMarker(pos, markerTitle, true, true, markerRadius);
            if (mListener != null) {
                mListener.placeChanged(pos, getFormattedAddress(sel));
            }
        });
        if (isPlaces) loadPlaces();
        return binding.getRoot();
    }

    public void setOnMarkerClick(GoogleMap.OnMarkerClickListener onMarkerClickListener) {
        this.onMarkerClickListener = onMarkerClickListener;
        if (mMap != null) mMap.setOnMarkerClickListener(onMarkerClickListener);
    }

    public void setOnMapClickListener(GoogleMap.OnMapClickListener onMapClickListener) {
        this.onMapClickListener = onMapClickListener;
        if (mMap != null) mMap.setOnMapClickListener(onMapClickListener);
    }

    private String getFormattedAddress(Address address) {
        return String.format("%s, %s%s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getMaxAddressLineIndex() > 1 ? address.getAddressLine(1) + ", " : "",
                address.getCountryName());
    }

    private void initViews() {
        groupOne = binding.groupOne;
        groupTwo = binding.groupTwo;
        groupThree = binding.groupThree;
        emptyItem = binding.emptyItem;
        placesList = binding.placesList;
        placesList.setLayoutManager(new LinearLayoutManager(getContext()));

        CardView zoomCard = binding.zoomCard;
        CardView searchCard = binding.searchCard;
        CardView myCard = binding.myCard;
        CardView layersCard = binding.layersCard;
        CardView placesCard = binding.placesCard;
        CardView backCard = binding.backCard;
        styleCard = binding.styleCard;
        placesListCard = binding.placesListCard;
        CardView markersCard = binding.markersCard;
        placesListCard.setVisibility(View.GONE);
        styleCard.setVisibility(View.GONE);
        zoomCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        searchCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        myCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        layersCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        placesCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        styleCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        placesListCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        markersCard.setCardBackgroundColor(getThemeUtil().getCardStyle());
        backCard.setCardBackgroundColor(getThemeUtil().getCardStyle());

        layersContainer = binding.layersContainer;
        layersContainer.setVisibility(View.GONE);
        layersContainer.setCardBackgroundColor(getThemeUtil().getCardStyle());

        if (Module.isLollipop()) {
            zoomCard.setCardElevation(Configs.CARD_ELEVATION);
            searchCard.setCardElevation(Configs.CARD_ELEVATION);
            myCard.setCardElevation(Configs.CARD_ELEVATION);
            layersContainer.setCardElevation(Configs.CARD_ELEVATION);
            layersCard.setCardElevation(Configs.CARD_ELEVATION);
            placesCard.setCardElevation(Configs.CARD_ELEVATION);
            styleCard.setCardElevation(Configs.CARD_ELEVATION);
            placesListCard.setCardElevation(Configs.CARD_ELEVATION);
            markersCard.setCardElevation(Configs.CARD_ELEVATION);
            backCard.setCardElevation(Configs.CARD_ELEVATION);
        }

        int style = getThemeUtil().getCardStyle();
        zoomCard.setCardBackgroundColor(style);
        searchCard.setCardBackgroundColor(style);
        myCard.setCardBackgroundColor(style);
        layersContainer.setCardBackgroundColor(style);
        layersCard.setCardBackgroundColor(style);
        placesCard.setCardBackgroundColor(style);
        styleCard.setCardBackgroundColor(style);
        placesListCard.setCardBackgroundColor(style);
        markersCard.setCardBackgroundColor(style);
        backCard.setCardBackgroundColor(style);

        ImageButton cardClear = binding.cardClear;
        zoomOut = binding.mapZoom;
        ImageButton layers = binding.layers;
        ImageButton myLocation = binding.myLocation;
        markers = binding.markers;
        places = binding.places;
        backButton = binding.backButton;

        cardClear.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        layers.setOnClickListener(this);
        myLocation.setOnClickListener(this);
        markers.setOnClickListener(this);
        places.setOnClickListener(this);
        backButton.setOnClickListener(this);

        binding.typeNormal.setOnClickListener(this);
        binding.typeSatellite.setOnClickListener(this);
        binding.typeHybrid.setOnClickListener(this);
        binding.typeTerrain.setOnClickListener(this);

        if (!isPlaces) {
            placesCard.setVisibility(View.GONE);
        }
        if (!isBack) {
            backCard.setVisibility(View.GONE);
        }
        if (!isSearch) {
            searchCard.setVisibility(View.GONE);
        }
        if (!isStyles || !Module.isPro()) {
            markersCard.setVisibility(View.GONE);
        }
        if (!isZoom) {
            zoomCard.setVisibility(View.GONE);
        }
        loadMarkers();
    }

    private void loadMarkers() {
        groupOne.removeAllViewsInLayout();
        groupTwo.removeAllViewsInLayout();
        groupThree.removeAllViewsInLayout();
        for (int i = 0; i < ThemeUtil.NUM_OF_MARKERS; i++) {
            ImageButton ib = new ImageButton(getContext());
            ib.setBackgroundResource(android.R.color.transparent);
            ib.setImageResource(getThemeUtil().getMarkerStyle(i));
            ib.setId(i + ThemeUtil.NUM_OF_MARKERS);
            ib.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    MeasureUtils.dp2px(getContext(), 35),
                    MeasureUtils.dp2px(getContext(), 35));
            int px = MeasureUtils.dp2px(getContext(), 2);
            params.setMargins(px, px, px, px);
            ib.setLayoutParams(params);
            if (i < 5) {
                groupOne.addView(ib);
            } else if (i < 10) {
                groupTwo.addView(ib);
            } else {
                groupThree.addView(ib);
            }
        }
    }

    private void setMapType(int type) {
        if (mMap != null) {
            mMap.setMapType(type);
            getPrefs().setMapType(type);
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void setMyLocation() {
        if (!Permissions.checkPermission(getContext(), Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(getContext(), 205, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void loadPlaces() {
        if (placeRecyclerAdapter == null) {
            List<PlaceItem> list = RealmDb.getInstance().getAllPlaces();
            if (list.isEmpty()) {
                binding.placesCard.setVisibility(View.GONE);
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            } else {
                emptyItem.setVisibility(View.GONE);
                binding.placesCard.setVisibility(View.VISIBLE);
                placesList.setVisibility(View.VISIBLE);
                placeRecyclerAdapter = new PlacesRecyclerAdapter(getContext(), list, new SimpleListener() {
                    @Override
                    public void onItemClicked(int position, View view) {
                        hideLayers();
                        hidePlaces();
                        PlaceItem item = placeRecyclerAdapter.getItem(position);
                        if (item != null) {
                            addMarker(new LatLng(item.getLat(), item.getLng()), markerTitle, true, true, markerRadius);
                        }
                    }

                    @Override
                    public void onItemLongClicked(int position, View view) {

                    }
                }, null);
                placesList.setAdapter(placeRecyclerAdapter);
            }
        } else {
            if (placeRecyclerAdapter.getItemCount() > 0) {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
                placesList.setAdapter(placeRecyclerAdapter);
                addMarkers(placeRecyclerAdapter.getUsedData());
            } else {
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addMarkers(List<PlaceItem> list) {
        if (list != null && list.size() > 0) {
            for (PlaceItem model : list) {
                addMarker(new LatLng(model.getLat(), model.getLng()), model.getTitle(), false,
                        model.getIcon(), false, model.getRadius());
            }
        }
    }

    private void toggleMarkers() {
        if (isLayersVisible()) {
            hideLayers();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        }
        if (isMarkersVisible()) {
            hideStyles();
        } else {
            ViewUtils.slideInUp(getContext(), styleCard);
        }
    }

    private void hideStyles() {
        if (isMarkersVisible()) {
            ViewUtils.slideOutDown(getContext(), styleCard);
        }
    }

    private boolean isMarkersVisible() {
        return styleCard != null && styleCard.getVisibility() == View.VISIBLE;
    }

    private void togglePlaces() {
        if (isMarkersVisible()) {
            hideStyles();
        }
        if (isLayersVisible()) {
            hideLayers();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        } else {
            ViewUtils.slideInUp(getContext(), placesListCard);
        }
    }

    private void hidePlaces() {
        if (isPlacesVisible()) {
            ViewUtils.slideOutDown(getContext(), placesListCard);
        }
    }

    private boolean isPlacesVisible() {
        return placesListCard != null && placesListCard.getVisibility() == View.VISIBLE;
    }

    private void toggleLayers() {
        if (isMarkersVisible()) {
            hideStyles();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        }
        if (isLayersVisible()) {
            hideLayers();
        } else {
            ViewUtils.showOver(layersContainer);
        }
    }

    private void hideLayers() {
        if (isLayersVisible()) {
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void zoomClick() {
        isFullscreen = !isFullscreen;
        if (mListener != null) {
            mListener.onZoomClick(isFullscreen);
        }
        if (isFullscreen) {
            if (isDark) zoomOut.setImageResource(R.drawable.ic_arrow_downward_white_24dp);
            else zoomOut.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
        } else {
            restoreScaleButton();
        }
    }

    private boolean isLayersVisible() {
        return layersContainer != null && layersContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 205:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMyLocation();
                } else {
                    Toast.makeText(getContext(), R.string.cant_access_location_services, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id >= ThemeUtil.NUM_OF_MARKERS && id < ThemeUtil.NUM_OF_MARKERS * 2) {
            recreateStyle(v.getId() - ThemeUtil.NUM_OF_MARKERS);
            hideStyles();
        }
        switch (id) {
            case R.id.cardClear:
                cardSearch.setText("");
                break;
            case R.id.mapZoom:
                zoomClick();
                break;
            case R.id.layers:
                toggleLayers();
                break;
            case R.id.myLocation:
                hideLayers();
                moveToMyLocation();
                break;
            case R.id.typeNormal:
                setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.typeHybrid:
                setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.typeSatellite:
                setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.typeTerrain:
                setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.places:
                togglePlaces();
                break;
            case R.id.markers:
                toggleMarkers();
                break;
            case R.id.backButton:
                restoreScaleButton();
                if (mListener != null) {
                    mListener.onBackClick();
                }
                break;
        }
    }

    private void restoreScaleButton() {
        if (isDark) zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
        else zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
    }
}
