package com.elementary.tasks.places;

import android.content.ContentResolver;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.fragments.AdvancedMapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.UUID;

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

public class CreatePlaceActivity extends ThemedActivity implements MapListener, MapCallback {

    private static final int MENU_ITEM_DELETE = 12;

    private ActivityCreatePlaceBinding binding;
    private AdvancedMapFragment mGoogleMap;
    private PlaceItem mItem;
    private LatLng place;
    private String placeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPlace();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_place);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false,
                getPrefs().getMarkerStyle(), getThemeUtil().isDark());
        mGoogleMap.setListener(this);
        mGoogleMap.setCallback(this);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mGoogleMap)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected String getStats() {
        return "Place " + (mItem != null ? "edit" : "create");
    }

    private void loadPlace() {
        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.INTENT_ID);
        if (id != null) {
            mItem = RealmDb.getInstance().getPlace(id);
        } else if (intent.getData() != null) {
            try {
                Uri name = intent.getData();
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mItem = BackupTool.getInstance().getPlace(cr, name);
                } else {
                    mItem = BackupTool.getInstance().getPlace(name.getPath(), null);
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPlace() {
        if (mItem != null) {
            mGoogleMap.addMarker(new LatLng(mItem.getLat(), mItem.getLng()), mItem.getTitle(), true, true, -1);
            binding.placeName.setText(mItem.getTitle());
        }
    }

    private void addPlace() {
        if (place != null) {
            String name = binding.placeName.getText().toString().trim();
            if (name.matches("")) {
                name = placeTitle;
            }
            if (name == null || name.matches("")) {
                binding.placeName.setError(getString(R.string.must_be_not_empty));
                return;
            }
            Double latitude = place.latitude;
            Double longitude = place.longitude;
            if (mItem != null) {
                mItem.setTitle(name);
                mItem.setLat(latitude);
                mItem.setLng(longitude);
            } else {
                mItem = new PlaceItem(name, UUID.randomUUID().toString(), latitude, longitude, 0, getPrefs().getRadius());
            }
            RealmDb.getInstance().saveObject(mItem);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.you_dont_select_place), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add:
                addPlace();
                return true;
            case MENU_ITEM_DELETE:
                deleteItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteItem() {
        if (mItem != null) {
            RealmDb.getInstance().deletePlace(mItem);
            new DeletePlaceFilesAsync(this).execute(mItem.getKey());
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mItem != null && getPrefs().isAutoSaveEnabled()) {
            addPlace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_palce_edit, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void placeChanged(LatLng place, String address) {
        this.place = place;
        placeTitle = address;
    }

    @Override
    public void onBackClick() {

    }

    @Override
    public void onZoomClick(boolean isFull) {

    }

    @Override
    public void onMapReady() {
        showPlace();
    }
}
