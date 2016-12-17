package com.elementary.tasks.core.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.QuickReturnUtils;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.AddressAutoCompleteView;
import com.elementary.tasks.databinding.FragmentMapBinding;
import com.elementary.tasks.places.PlaceItem;
import com.elementary.tasks.places.PlacesRecyclerAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
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

public class MapFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MapFragment";

    private Activity mContext;

    private GoogleMap mMap;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private AddressAutoCompleteView cardSearch;
    private ImageButton zoomOut;
    private ImageButton backButton;
    private ImageButton places;
    private ImageButton markers;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;
    private FragmentMapBinding binding;

    private ArrayList<String> spinnerArray = new ArrayList<>();
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

    private ThemeUtil mColor;

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
                if (isTouch) {
                    addMarker(latLng, markerTitle, true, true, markerRadius);
                }
            });

            if (lastPos != null) {
                addMarker(lastPos, lastPos.toString(), true, false, markerRadius);
            }
            if (mCallback != null) {
                mCallback.onMapReady();
            }
        }
    };

    public static MapFragment newInstance(boolean isTouch, boolean isPlaces,
                                          boolean isSearch, boolean isStyles,
                                          boolean isBack, boolean isZoom, boolean isDark) {
        MapFragment fragment = new MapFragment();
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

    public static MapFragment newInstance(boolean isPlaces, boolean isStyles, boolean isBack,
                                          boolean isZoom, int markerStyle, boolean isDark) {
        MapFragment fragment = new MapFragment();
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

    public MapFragment() {

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
                markerRadius = Prefs.getInstance(mContext).getRadius();
            if (clear) mMap.clear();
            if (title == null || title.matches("")) title = pos.toString();
            if (!Module.isPro()) markerStyle = 5;
            lastPos = pos;
            if (mListener != null) mListener.placeChanged(pos, title);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(mColor.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(mColor.getColor(circleColors[0]))
                    .strokeColor(mColor.getColor(circleColors[1])));
            if (animate) animate(pos);
        }
    }

    private BitmapDescriptor getDescriptor(int resId) {
        if (Module.isLollipop()) {
            return getBitmapDescriptor(resId);
        } else {
            return BitmapDescriptorFactory.fromResource(resId);
        }
    }

    private float convertDpToPixel(float dp) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private BitmapDescriptor getBitmapDescriptor(int id) {
        Drawable vectorDrawable = mContext.getDrawable(id);
        int h = ((int) convertDpToPixel(24));
        int w = ((int) convertDpToPixel(24));
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public void addMarker(LatLng pos, String title, boolean clear, int markerStyle, boolean animate, int radius) {
        if (mMap != null) {
            markerRadius = radius;
            if (markerRadius == -1) {
                markerRadius = Prefs.getInstance(mContext).getRadius();
            }
            if (!Module.isPro()) markerStyle = 5;
            this.markerStyle = markerStyle;
            if (clear) mMap.clear();
            if (title == null || title.matches(""))
                title = pos.toString();
            lastPos = pos;
            if (mListener != null) mListener.placeChanged(pos, title);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(getDescriptor(mColor.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(mColor.getColor(circleColors[0]))
                    .strokeColor(mColor.getColor(circleColors[1])));
            if (animate) animate(pos);
        } else {
            Log.d(TAG, "map is null");
        }
    }

    public void recreateMarker(int radius) {
        markerRadius = radius;
        if (markerRadius == -1)
            markerRadius = Prefs.getInstance(mContext).getRadius();
        if (mMap != null && lastPos != null) {
            mMap.clear();
            if (markerTitle == null || markerTitle.matches(""))
                markerTitle = lastPos.toString();
            if (mListener != null) mListener.placeChanged(lastPos, markerTitle);
            if (!Module.isPro()) markerStyle = 5;
            mMap.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(mColor.getMarkerStyle(markerStyle)))
                    .draggable(true));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(lastPos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(mColor.getColor(circleColors[0]))
                    .strokeColor(mColor.getColor(circleColors[1])));
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
                    .icon(getDescriptor(mColor.getMarkerStyle(markerStyle)))
                    .draggable(true));
            if (markerStyle >= 0) {
                int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
                if (markerRadius == -1) {
                    markerRadius = Prefs.getInstance(mContext).getRadius();
                }
                mMap.addCircle(new CircleOptions()
                        .center(lastPos)
                        .radius(markerRadius)
                        .strokeWidth(strokeWidth)
                        .fillColor(mColor.getColor(circleColors[0]))
                        .strokeColor(mColor.getColor(circleColors[1])));
            }
            animate(lastPos);
        }
    }

    public void moveCamera(LatLng pos) {
        if (mMap != null) animate(pos);
    }

    public void animate(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (mMap != null) mMap.animateCamera(update);
    }

    public void moveToMyLocation() {
        if (mMap != null) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                animate(pos);
            } else {
                location = mMap.getMyLocation();
                if (location != null) {
                    LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                    animate(pos);
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
        if (mContext == null) {
            return;
        }
        if (!Prefs.getInstance(mContext).isMapShowcase() && isBack) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(mColor.getColor(mColor.colorAccent()));
            config.setContentTextColor(mColor.getColor(R.color.whitePrimary));
            config.setDismissTextColor(mColor.getColor(R.color.whitePrimary));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(mContext);
            sequence.setConfig(config);
            sequence.addSequenceItem(zoomOut,
                    mContext.getString(R.string.click_to_expand_collapse_map),
                    mContext.getString(R.string.got_it));
            sequence.addSequenceItem(backButton,
                    mContext.getString(R.string.click_when_add_place),
                    mContext.getString(R.string.got_it));
            if (Module.isPro()) {
                sequence.addSequenceItem(markers,
                        mContext.getString(R.string.select_style_for_marker),
                        mContext.getString(R.string.got_it));
            }
            sequence.addSequenceItem(places,
                    mContext.getString(R.string.select_place_from_list),
                    mContext.getString(R.string.got_it));
            sequence.start();
            Prefs.getInstance(mContext).setMapShowcase(true);
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
            markerStyle = args.getInt(MARKER_STYLE, Prefs.getInstance(mContext).getMarkerStyle());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initArgs();
        mColor = ThemeUtil.getInstance(mContext);
        binding = FragmentMapBinding.inflate(inflater, container, false);
        final Prefs prefs = Prefs.getInstance(mContext);
        markerRadius = prefs.getRadius();
        mMapType = prefs.getMapType();
        if (!Module.isPro()) {
            markerStyle = prefs.getMarkerStyle();
        }
        isDark = mColor.isDark();
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
        ImageView emptyImage = binding.emptyImage;
        if (isDark) {
            emptyImage.setImageResource(R.drawable.ic_directions_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_directions_black_24dp);
        }
        placesList = binding.placesList;
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
        zoomCard.setCardBackgroundColor(mColor.getCardStyle());
        searchCard.setCardBackgroundColor(mColor.getCardStyle());
        myCard.setCardBackgroundColor(mColor.getCardStyle());
        layersCard.setCardBackgroundColor(mColor.getCardStyle());
        placesCard.setCardBackgroundColor(mColor.getCardStyle());
        styleCard.setCardBackgroundColor(mColor.getCardStyle());
        placesListCard.setCardBackgroundColor(mColor.getCardStyle());
        markersCard.setCardBackgroundColor(mColor.getCardStyle());
        backCard.setCardBackgroundColor(mColor.getCardStyle());

        layersContainer = binding.layersContainer;
        layersContainer.setVisibility(View.GONE);
        layersContainer.setCardBackgroundColor(mColor.getCardStyle());

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

        int style = mColor.getCardStyle();
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

        if (isDark) {
            cardClear.setImageResource(R.drawable.ic_clear_white_24dp);
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
            layers.setImageResource(R.drawable.ic_layers_white_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_white_24dp);
            markers.setImageResource(R.drawable.ic_palette_white_24dp);
            places.setImageResource(R.drawable.ic_directions_white_24dp);
            backButton.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp);
        } else {
            cardClear.setImageResource(R.drawable.ic_clear_black_24dp);
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
            layers.setImageResource(R.drawable.ic_layers_black_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_black_24dp);
            markers.setImageResource(R.drawable.ic_palette_black_24dp);
            places.setImageResource(R.drawable.ic_directions_black_24dp);
            backButton.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
        }

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
            ImageButton ib = new ImageButton(mContext);
            ib.setBackgroundResource(android.R.color.transparent);
            ib.setImageResource(mColor.getMarkerStyle(i));
            ib.setId(i + ThemeUtil.NUM_OF_MARKERS);
            ib.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    QuickReturnUtils.dp2px(mContext, 35),
                    QuickReturnUtils.dp2px(mContext, 35));
            int px = QuickReturnUtils.dp2px(mContext, 2);
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
            Prefs.getInstance(mContext).setMapType(type);
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void setMyLocation() {
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Permissions.requestPermission(mContext, 205,
                    Permissions.ACCESS_FINE_LOCATION,
                    Permissions.ACCESS_COARSE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void loadPlaces(){
        if (placeRecyclerAdapter == null) {
            List<PlaceItem> list = RealmDb.getInstance().getAllPlaces();
            spinnerArray = new ArrayList<>();
            spinnerArray.clear();
            for (PlaceItem item : list) {
                spinnerArray.add(item.getTitle());
            }
            if (spinnerArray.isEmpty()) {
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            } else {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
//                PlaceAdapter adapter = new PlaceAdapter(mContext, spinnerArray);
//                adapter.setEventListener(new SimpleListener() {
//                    @Override
//                    public void onItemClicked(int position, View view) {
//                        hideLayers();
//                        hidePlaces();
//                        String placeName = spinnerArray.get(position);
//                        PlaceItem item = PlacesHelper.getInstance(mContext).getPlace(placeName);
//                        if (item != null) {
//                            addMarker(item.getPosition(), markerTitle, true, true, markerRadius);
//                        }
//                    }
//
//                    @Override
//                    public void onItemLongClicked(int position, View view) {
//
//                    }
//                });
//                placesList.setLayoutManager(new LinearLayoutManager(mContext));
//                placesList.setAdapter(adapter);
            }
        } else {
            if (placeRecyclerAdapter.getItemCount() > 0) {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
                placesList.setLayoutManager(new LinearLayoutManager(mContext));
                placesList.setAdapter(placeRecyclerAdapter);
//                addMarkers(placeRecyclerAdapter.getData());
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
            ViewUtils.slideInUp(mContext, styleCard);
        }
    }

    private void hideStyles() {
        if (isMarkersVisible()) {
            ViewUtils.slideOutDown(mContext, styleCard);
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
            ViewUtils.slideInUp(mContext, placesListCard);
        }
    }

    private void hidePlaces() {
        if (isPlacesVisible()) {
            ViewUtils.slideOutDown(mContext, placesListCard);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 205:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setMyLocation();
                } else {
                    Toast.makeText(mContext, R.string.cant_access_location_services, Toast.LENGTH_SHORT).show();
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
        if (isDark) {
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
        } else {
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        }
    }
}
