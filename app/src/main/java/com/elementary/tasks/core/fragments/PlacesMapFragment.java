package com.elementary.tasks.core.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.location.LocationTracker;
import com.elementary.tasks.core.network.Api;
import com.elementary.tasks.core.network.places.PlacesResponse;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.ThemedImageButton;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.databinding.FragmentPlacesMapBinding;
import com.elementary.tasks.places.GooglePlaceItem;
import com.elementary.tasks.places.GooglePlacesAdapter;
import com.elementary.tasks.places.PlaceParser;
import com.elementary.tasks.places.RequestBuilder;
import com.elementary.tasks.reminder.models.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

public class PlacesMapFragment extends BaseMapFragment implements View.OnClickListener {

    private static final String SHOWCASE = "places_showcase";

    private FragmentPlacesMapBinding binding;
    private GoogleMap mMap;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private RoboEditText cardSearch;
    private ThemedImageButton zoomOut;
    private ThemedImageButton places;
    private ThemedImageButton markers;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;

    private List<GooglePlaceItem> spinnerArray = new ArrayList<>();

    private boolean isZoom = true;
    private boolean isFullscreen = false;
    private boolean isDark = false;
    private int mRadius = -1;
    private int markerStyle = -1;
    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;
    private double mLat, mLng;

    private LocationTracker mLocList;

    private MapListener mMapListener;
    private MapCallback mCallback;

    public static final String ENABLE_ZOOM = "enable_zoom";
    public static final String MARKER_STYLE = "marker_style";
    public static final String THEME_MODE = "theme_mode";

    private Call<PlacesResponse> call;

    private OnMapReadyCallback mMapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setMapType(mMapType);
            setMyLocation();
            mMap.setOnMapClickListener(latLng -> {
                hideLayers();
                hidePlaces();
                hideStyles();
            });
            if (mCallback != null) {
                mCallback.onMapReady();
            }
        }
    };
    private LocationTracker.Callback mTrackerCallback = (lat, lon) -> {
        mLat = lat;
        mLng = lon;
    };
    private Callback<PlacesResponse> mSearchCallback = new Callback<PlacesResponse>() {
        @Override
        public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
            if (response.code() == Api.OK) {
                List<GooglePlaceItem> places = new ArrayList<>();
                for (com.elementary.tasks.core.network.places.Place place : response.body().getResults()) {
                    places.add(PlaceParser.getDetails(place));
                }
                spinnerArray = places;
                if (spinnerArray.size() == 0) {
                    Toast.makeText(getContext(), SuperUtil.getString(PlacesMapFragment.this, R.string.no_places_found), Toast.LENGTH_SHORT).show();
                }
                addSelectAllItem();
                refreshAdapter(true);
            } else {
                Toast.makeText(getContext(), SuperUtil.getString(PlacesMapFragment.this, R.string.no_places_found), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<PlacesResponse> call, Throwable t) {
            Toast.makeText(getContext(), SuperUtil.getString(PlacesMapFragment.this, R.string.no_places_found), Toast.LENGTH_SHORT).show();
        }
    };

    public static PlacesMapFragment newInstance(boolean isZoom, boolean isDark) {
        PlacesMapFragment fragment = new PlacesMapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        fragment.setArguments(args);
        return fragment;
    }

    public static PlacesMapFragment newInstance(boolean isZoom, int markerStyle, boolean isDark) {
        PlacesMapFragment fragment = new PlacesMapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        args.putInt(MARKER_STYLE, markerStyle);
        fragment.setArguments(args);
        return fragment;
    }

    public PlacesMapFragment() {
    }

    public void setListener(MapListener listener) {
        this.mMapListener = listener;
    }

    public void setCallback(MapCallback callback) {
        this.mCallback = callback;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public void setMarkerStyle(int markerStyle) {
        this.markerStyle = markerStyle;
    }

    public void addMarker(LatLng pos, String title, boolean clear, boolean animate, int radius) {
        if (mMap != null && pos != null) {
            if (pos.latitude == 0.0 && pos.longitude == 0.0) return;
            mRadius = radius;
            if (mRadius == -1) {
                mRadius = getPrefs().getRadius();
            }
            if (clear) {
                mMap.clear();
            }
            if (title == null || title.matches("")) {
                title = pos.toString();
            }
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(getThemeUtil().getMarkerStyle(markerStyle)))
                    .draggable(clear));
            ThemeUtil.Marker marker = getThemeUtil().getMarkerRadiusStyle(markerStyle);
            float strokeWidth = 3f;
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(mRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(getThemeUtil().getColor(marker.getFillColor()))
                    .strokeColor(getThemeUtil().getColor(marker.getStrokeColor())));
            if (animate) {
                animate(pos);
            }
        }
    }

    public void recreateMarker(int radius) {
        mRadius = radius;
        if (mRadius == -1) {
            mRadius = getPrefs().getRadius();
        }
        if (mMap != null) {
            addMarkers();
        }
    }

    public void recreateStyle(int style) {
        markerStyle = style;
        if (mMap != null) {
            addMarkers();
        }
    }

    public void addMarkers(List<Place> list) {
        mMap.clear();
        toModels(list, false);
        addSelectAllItem();
        refreshAdapter(false);
    }

    private void addSelectAllItem() {
        if (spinnerArray != null && spinnerArray.size() > 1) {
            spinnerArray.add(new GooglePlaceItem(SuperUtil.getString(PlacesMapFragment.this, R.string.add_all), null, null, null, null, null, false));
        }
    }

    public void selectMarkers(List<Place> list) {
        mMap.clear();
        toModels(list, true);
        refreshAdapter(false);
    }

    public void animate(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (mMap != null) {
            mMap.animateCamera(update);
        }
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
        if (!getPrefs().isShowcase(SHOWCASE)) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(getThemeUtil().getColor(getThemeUtil().colorAccent()));
            config.setContentTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            config.setDismissTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getContext());
            sequence.setConfig(config);
            sequence.addSequenceItem(zoomOut,
                    SuperUtil.getString(PlacesMapFragment.this, R.string.click_to_expand_collapse_map),
                    SuperUtil.getString(PlacesMapFragment.this, R.string.got_it));
            sequence.addSequenceItem(markers,
                    SuperUtil.getString(PlacesMapFragment.this, R.string.select_style_for_marker),
                    SuperUtil.getString(PlacesMapFragment.this, R.string.got_it));
            sequence.start();
            getPrefs().setShowcase(SHOWCASE, true);
        }
    }

    private void initArgs() {
        Bundle args = getArguments();
        if (args != null) {
            isZoom = args.getBoolean(ENABLE_ZOOM, true);
            isDark = args.getBoolean(THEME_MODE, false);
            markerStyle = args.getInt(MARKER_STYLE, getPrefs().getMarkerStyle());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initArgs();
        binding = FragmentPlacesMapBinding.inflate(inflater, container, false);
        mRadius = getPrefs().getRadius();
        mMapType = getPrefs().getMapType();
        isDark = getThemeUtil().isDark();

        com.google.android.gms.maps.MapFragment fragment = com.google.android.gms.maps.MapFragment.newInstance();
        fragment.getMapAsync(mMapCallback);
        getFragmentManager().beginTransaction()
                .add(binding.mapPlaces.getId(), fragment)
                .commit();

        initViews();
        cardSearch = binding.cardSearch;
        cardSearch.setHint(R.string.search_place);
        cardSearch.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                hideKeyboard();
                loadPlaces();
                return true;
            }
            return false;
        });
        return binding.getRoot();
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
        markers = binding.markers;
        places = binding.places;

        cardClear.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        layers.setOnClickListener(this);
        markers.setOnClickListener(this);
        places.setOnClickListener(this);

        binding.typeNormal.setOnClickListener(this);
        binding.typeSatellite.setOnClickListener(this);
        binding.typeHybrid.setOnClickListener(this);
        binding.typeTerrain.setOnClickListener(this);

        backCard.setVisibility(View.GONE);
        if (!Module.isPro()) {
            markersCard.setVisibility(View.GONE);
        }
        if (!isZoom) {
            zoomCard.setVisibility(View.GONE);
        }
        loadMarkers();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardSearch.getWindowToken(), 0);
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
        String req = cardSearch.getText().toString().trim().toLowerCase();
        if (req.matches("")) return;
        cancelSearchTask();
        call = RequestBuilder.getSearch(req);
        if (mLat != 0.0 && mLng != 0.0) {
            call = RequestBuilder.getNearby(mLat, mLng, req);
        }
        call.enqueue(mSearchCallback);
    }

    private void cancelSearchTask() {
        if (call != null && !call.isExecuted()) {
            call.cancel();
        }
    }

    private void refreshAdapter(boolean show) {
        GooglePlacesAdapter placesAdapter = new GooglePlacesAdapter(getContext(), spinnerArray);
        placesAdapter.setEventListener(new SimpleListener() {
            @Override
            public void onItemClicked(int position, View view) {
                hideLayers();
                hidePlaces();
                animate(spinnerArray.get(position).getPosition());
            }

            @Override
            public void onItemLongClicked(int position, View view) {

            }
        });
        if (spinnerArray != null && spinnerArray.size() > 0) {
            emptyItem.setVisibility(View.GONE);
            placesList.setVisibility(View.VISIBLE);
            placesList.setAdapter(placesAdapter);
            addMarkers();
            if (!isPlacesVisible() && show) ViewUtils.slideInUp(getContext(), placesListCard);
        } else {
            placesList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    public List<Place> getPlaces() {
        List<Place> places = new ArrayList<>();
        if (spinnerArray != null && spinnerArray.size() > 0) {
            for (GooglePlaceItem model : spinnerArray) {
                if (model.isSelected()) {
                    if (model.getPosition() != null) {
                        places.add(new Place(mRadius, markerStyle, model.getPosition().latitude,
                                model.getPosition().longitude, model.getName(), model.getAddress(), model.getTypes()));
                    }
                }
            }
        }
        return places;
    }

    private void toModels(List<Place> list, boolean select) {
        spinnerArray = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (Place model : list) {
                spinnerArray.add(new GooglePlaceItem(model.getName(), model.getId(),
                        null, model.getAddress(), new LatLng(model.getLatitude(),
                        model.getLongitude()), model.getTags(), select));
            }
        }
    }

    private void addMarkers() {
        mMap.clear();
        if (spinnerArray != null && spinnerArray.size() > 0) {
            for (GooglePlaceItem model : spinnerArray) {
                addMarker(model.getPosition(), model.getName(), false, false, mRadius);
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
        if (mMapListener != null) {
            mMapListener.onZoomClick(isFullscreen);
        }
        if (isFullscreen) {
            if (isDark) zoomOut.setImageResource(R.drawable.ic_arrow_downward_white_24dp);
            else zoomOut.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
        } else {
            if (isDark) zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
            else zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        }
    }

    private boolean isLayersVisible() {
        return layersContainer != null && layersContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTracking();
    }

    private void startTracking() {
        mLocList = new LocationTracker(getContext(), mTrackerCallback);
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
            case 200:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracking();
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
                loadPlaces();
                break;
            case R.id.mapZoom:
                zoomClick();
                break;
            case R.id.layers:
                toggleLayers();
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTracking();
    }

    private void cancelTracking() {
        if (mLocList != null) {
            mLocList.removeUpdates();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelTracking();
        cancelSearchTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTracking();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelTracking();
    }
}
