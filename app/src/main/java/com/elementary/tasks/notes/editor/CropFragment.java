package com.elementary.tasks.notes.editor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.CropFragmentBinding;
import com.elementary.tasks.notes.NoteImage;

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

public class CropFragment extends BitmapFragment {

    private static final String IMAGE = "image";

    private CropFragmentBinding binding;
    private NoteImage mItem;

    public static CropFragment newInstance(NoteImage image) {
        CropFragment fragment = new CropFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(IMAGE, image);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItem = (NoteImage) getArguments().getSerializable(IMAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = CropFragmentBinding.inflate(inflater, container, false);
        binding.background.setBackgroundColor(ThemeUtil.getInstance(getContext()).getBackgroundStyle());
        initControls();
        loadImage();
        return binding.getRoot();
    }

    private void loadImage() {
        Glide.with(this)
                .load(mItem.getImage())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        binding.cropImageView.setImageBitmap(resource);
                    }
                });
    }

    private void initControls() {
        binding.rotateLeftButton.setOnClickListener(view -> binding.cropImageView.rotateImage(-90));
        binding.rotateRightButton.setOnClickListener(view -> binding.cropImageView.rotateImage(90));
    }

    @Override
    public NoteImage getImage() {
        Bitmap cropped = binding.cropImageView.getCroppedImage();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        mItem.setImage(outputStream.toByteArray());
        return mItem;
    }

    @Override
    public NoteImage getOriginalImage() {
        return mItem;
    }
}
