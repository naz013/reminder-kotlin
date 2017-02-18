package com.elementary.tasks.navigation.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.fragments.AdvancedMapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.BottomSheetLayoutBinding;
import com.elementary.tasks.databinding.FragmentEventsMapBinding;
import com.elementary.tasks.places.LocationPlacesAdapter;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

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

public class MapFragment extends BaseNavigationFragment {

    private FragmentEventsMapBinding binding;

    private AdvancedMapFragment mGoogleMap;
    private RecyclerView mEventsList;
    private LinearLayout mEmptyItem;

    private List<Reminder> mData = new ArrayList<>();
    private int clickedPosition;
    private int pointer;
    private boolean isDataShowed;

    private MapCallback mReadyCallback = new MapCallback() {
        @Override
        public void onMapReady() {
            mGoogleMap.setSearchEnabled(false);
            showData();
        }
    };
    private BottomSheetBehavior.BottomSheetCallback mSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };
    private GoogleMap.OnMarkerClickListener mOnMarkerClick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            mGoogleMap.moveCamera(marker.getPosition(), 0, 0, 0, MeasureUtils.dp2px(mContext, 192));
            return false;
        }
    };
    private SimpleListener mClickListener = new SimpleListener() {
        @Override
        public void onItemClicked(int position, View view) {
            showClickedPlace(position);
        }

        @Override
        public void onItemLongClicked(int position, View view) {

        }
    };

    private void showClickedPlace(int position) {
        Reminder reminder = mData.get(position);
        int maxPointer = reminder.getPlaces().size() - 1;
        if (position != clickedPosition) {
            pointer = 0;
        } else {
            if (pointer == maxPointer) pointer = 0;
            else pointer++;
        }
        clickedPosition = position;
        Place place = reminder.getPlaces().get(pointer);
        mGoogleMap.moveCamera(new LatLng(place.getLatitude(), place.getLongitude()), 0, 0, 0, MeasureUtils.dp2px(mContext, 192));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsMapBinding.inflate(inflater, container, false);
        initMap();
        initViews();
        return binding.getRoot();
    }

    private void initMap() {
        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false,
                Prefs.getInstance(mContext).getMarkerStyle(), ThemeUtil.getInstance(mContext).isDark());
        mGoogleMap.setCallback(mReadyCallback);
        mGoogleMap.setOnMarkerClick(mOnMarkerClick);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mGoogleMap)
                .addToBackStack(null)
                .commit();
    }

    private void initViews() {
        BottomSheetLayoutBinding bottomSheet = binding.bottomSheet;
        mEventsList = bottomSheet.recyclerView;
        mEventsList.setLayoutManager(new LinearLayoutManager(mContext));
        mEmptyItem = bottomSheet.emptyItem;
        binding.sheetLayout.setBackgroundColor(ThemeUtil.getInstance(mContext).getCardStyle());
        BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from(binding.sheetLayout);
        mBottomSheetBehavior.setBottomSheetCallback(mSheetCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.map));
            mCallback.onFragmentSelect(this);
        }
        loadData();
        if (mGoogleMap != null) {
            showData();
        }
    }

    private void loadData() {
        mData = RealmDb.getInstance().getGpsReminders();
        isDataShowed = false;
    }

    private void showData() {
        if (isDataShowed) return;
        LocationPlacesAdapter mAdapter = new LocationPlacesAdapter(mContext, mData, mClickListener);
        mEventsList.setAdapter(mAdapter);
        boolean mapReady = false;
        for (Reminder reminder : mData) {
            for (Place place : reminder.getPlaces()) {
                mapReady = mGoogleMap.addMarker(new LatLng(place.getLatitude(), place.getLongitude()),
                        place.getName(), false, place.getMarker(), false, place.getRadius());
                if (!mapReady) break;
            }
            if (!mapReady) break;
        }
        isDataShowed = mapReady;
        reloadView();
    }

    private void reloadView() {
        RecyclerView.Adapter adapter = mEventsList.getAdapter();
        int size = 0;
        if (adapter != null) size = mEventsList.getAdapter().getItemCount();
        if (size > 0) {
            mEventsList.setVisibility(View.VISIBLE);
            mEmptyItem.setVisibility(View.GONE);
        } else {
            mEventsList.setVisibility(View.GONE);
            mEmptyItem.setVisibility(View.VISIBLE);
        }
    }
}
