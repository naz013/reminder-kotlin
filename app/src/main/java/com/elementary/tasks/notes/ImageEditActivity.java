package com.elementary.tasks.notes;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityImageEditBinding;

import java.io.ByteArrayOutputStream;

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
    private NoteImage mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItem = RealmDb.getInstance().getImage();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_edit);
        initActionBar();
        initControls();
        Glide.with(this)
                .load(mItem.getImage())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Log.d(TAG, "onResourceReady: ");
                        binding.cropImageView.setImageBitmap(resource);
                    }
                });
    }

    private void initControls() {
        binding.drawButton.setVisibility(View.GONE);
        if (themeUtil.isDark()) {
            binding.drawButton.setImageResource(R.drawable.ic_random_line);
            binding.rotateLeftButton.setImageResource(R.drawable.ic_rotate_left_white_24dp);
            binding.rotateRightButton.setImageResource(R.drawable.ic_rotate_right_white_24dp);
        } else {
            binding.drawButton.setImageResource(R.drawable.ic_random_line_black);
            binding.rotateLeftButton.setImageResource(R.drawable.ic_rotate_left_black_24dp);
            binding.rotateRightButton.setImageResource(R.drawable.ic_rotate_right_black_24dp);
        }
        binding.rotateLeftButton.setOnClickListener(view -> binding.cropImageView.rotateImage(-90));
        binding.rotateRightButton.setOnClickListener(view -> binding.cropImageView.rotateImage(90));
        binding.drawButton.setOnClickListener(view -> openDrawScreen());
    }

    private void openDrawScreen() {

    }

    private void initActionBar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.edit));
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
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_add:
                saveImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveImage() {
        Bitmap cropped = binding.cropImageView.getCroppedImage();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        mItem.setImage(outputStream.toByteArray());
        RealmDb.getInstance().saveImage(mItem);
        setResult(RESULT_OK);
        finish();
    }
}
