package com.elementary.tasks.notes.editor;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityImageEditBinding;

/**
 * Copyright 2017 Nazar Suhovich
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

public class ImageEditActivity extends ThemedActivity {

    private static final String TAG = "ImageEditActivity";

    private ActivityImageEditBinding binding;
    private BitmapFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageSingleton.getInstance().setItem(RealmDb.getInstance().getImage());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_edit);
        initActionBar();
        initTabControl();
    }

    private void initTabControl() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (binding.tabLayout.getTabAt(0) != null) {
            binding.tabLayout.getTabAt(0).select();
        }
        openCropFragment();
    }

    private void selectTab(int position) {
        if (fragment != null) {
            if (fragment instanceof CropFragment) {
                askCrop(position);
            } else if (fragment instanceof DrawFragment) {
                askDraw(position);
            }
        } else {
            switchTab(position);
        }
    }

    private void askDraw(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.which_image_you_want_to_use);
        builder.setPositiveButton(R.string.edited, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            ImageSingleton.getInstance().setItem(fragment.getImage());
            switchTab(position);
        });
        builder.setNegativeButton(R.string.original, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            ImageSingleton.getInstance().setItem(fragment.getOriginalImage());
            switchTab(position);
        });
        builder.create().show();
    }

    private void askCrop(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.which_image_you_want_to_use);
        builder.setPositiveButton(R.string.cropped, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            ImageSingleton.getInstance().setItem(fragment.getImage());
            switchTab(position);
        });
        builder.setNegativeButton(R.string.original, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            ImageSingleton.getInstance().setItem(fragment.getOriginalImage());
            switchTab(position);
        });
        builder.create().show();
    }

    private void switchTab(int position) {
        LogUtil.d(TAG, "switchTab: " + position);
        if (position == 1) {
            openDrawFragment();
        } else {
            openCropFragment();
        }
    }

    private void openDrawFragment() {
        replaceFragment(DrawFragment.newInstance());
    }

    private void openCropFragment() {
        replaceFragment(CropFragment.newInstance());
    }

    public void replaceFragment(BitmapFragment fragment) {
        this.fragment = fragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void initActionBar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.edit));
    }

    @Override
    public void onBackPressed() {
        if (fragment.onBackPressed()) {
            return;
        }
        closeScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_palce_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeScreen();
                return true;
            case R.id.action_add:
                saveImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeScreen() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fragment != null) fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Module.isMarshmallow() && fragment != null) fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveImage() {
        RealmDb.getInstance().saveImage(fragment.getImage());
        ImageSingleton.getInstance().setItem(null);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected String getStats() {
        return "Image edit";
    }
}
