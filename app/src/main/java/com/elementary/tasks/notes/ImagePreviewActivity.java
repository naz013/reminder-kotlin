package com.elementary.tasks.notes;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityImagePreviewBinding;

import java.util.ArrayList;
import java.util.Locale;

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

public class ImagePreviewActivity extends ThemedActivity {

    private ActivityImagePreviewBinding binding;
    private NoteItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview);
        initActionBar();
        initViewPager();
        setPhotoPosition();
    }

    private void setPhotoPosition() {
        int position = getIntent().getIntExtra(Constants.INTENT_POSITION, -1);
        if (position != -1)
            binding.photoPager.setCurrentItem(position);
    }

    private PhotoPagerAdapter getAdapter() {
        mItem = RealmDb.getInstance().getNote(getIntent().getStringExtra(Constants.INTENT_ID));
        if (mItem == null) {
            return new PhotoPagerAdapter(this, new ArrayList<>());
        } else {
            return new PhotoPagerAdapter(this, mItem.getImages());
        }
    }

    private void initViewPager() {
        binding.photoPager.setAdapter(getAdapter());
        binding.photoPager.setPageMargin(5);
        binding.photoPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setToolbarTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mItem != null) {
            setToolbarTitle(binding.photoPager.getCurrentItem());
        }
    }

    private void setToolbarTitle(int position) {
        binding.toolbar.setTitle(String.format(Locale.getDefault(), getString(R.string.x_out_of_x), position + 1, mItem.getImages().size()));
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (mItem != null && getIntent().getBooleanExtra(Constants.INTENT_DELETE, true)) {
            RealmDb.getInstance().deleteNote(mItem);
        }
        super.onDestroy();
    }
}
