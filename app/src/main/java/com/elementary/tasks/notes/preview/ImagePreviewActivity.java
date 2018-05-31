package com.elementary.tasks.notes.preview;

import android.os.Bundle;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.view_models.notes.NoteViewModel;
import com.elementary.tasks.databinding.ActivityImagePreviewBinding;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

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
    private NoteViewModel viewModel;
    @Nullable
    private Note mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview);
        initActionBar();

        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this, new NoteViewModel.Factory(getApplication(), getIntent().getStringExtra(Constants.INTENT_ID))).get(NoteViewModel.class);
        viewModel.note.observe(this, note -> {
            if (note != null) {
                initViewPager(note);
            }
        });
    }

    private void setPhotoPosition() {
        int position = getIntent().getIntExtra(Constants.INTENT_POSITION, -1);
        if (position != -1)
            binding.photoPager.setCurrentItem(position);
    }

    private PhotoPagerAdapter getAdapter(Note note) {
        if (note == null) {
            return new PhotoPagerAdapter(this, new ArrayList<>());
        } else {
            return new PhotoPagerAdapter(this, note.getImages());
        }
    }

    private void initViewPager(Note note) {
        this.mNote = note;
        binding.photoPager.setAdapter(getAdapter(note));
        binding.photoPager.setPageMargin(5);
        binding.photoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        if (note != null) {
            setToolbarTitle(binding.photoPager.getCurrentItem());
        }
        setPhotoPosition();
    }

    private void setToolbarTitle(int position) {
        binding.toolbar.setTitle(String.format(Locale.getDefault(), getString(R.string.x_out_of_x), position + 1, mNote.getImages().size()));
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
        super.onDestroy();
        if (mNote != null) viewModel.deleteNote(mNote);
    }
}
