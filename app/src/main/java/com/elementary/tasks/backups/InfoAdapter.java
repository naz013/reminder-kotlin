package com.elementary.tasks.backups;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.elementary.tasks.R;
import com.elementary.tasks.core.chart.PieSlice;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.databinding.BackupItemLayoutBinding;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

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

public class InfoAdapter {

    private static final String FILE_NAME = "Google_photo.jpg";

    private LinearLayout layout;
    private Context mContext;
    private ActionCallback mCallback;

    public InfoAdapter(LinearLayout layout, Context context, ActionCallback callback) {
        this.layout = layout;
        this.mContext = context;
        this.mCallback = callback;
        layout.removeAllViewsInLayout();
    }

    public void setData(List<UserItem> data) {
        if (layout == null) return;
        layout.removeAllViewsInLayout();
        for (UserItem userItem : data) {
            BackupItemLayoutBinding binding = getView();
            fillInfo(binding, userItem);
            layout.addView(binding.getRoot());
        }
    }

    private void fillInfo(BackupItemLayoutBinding binding, UserItem model) {
        if (model != null) {
            if (ThemeUtil.getInstance(mContext).isDark()) {
                binding.moreButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
            } else {
                binding.moreButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
            }
            binding.moreButton.setOnClickListener(view -> showPopup(model.kind, view));
            if (model.kind == UserInfoAsync.Info.Local) {
                binding.userContainer.setVisibility(View.GONE);
                binding.sourceName.setText(mContext.getString(R.string.local));
            } else {
                binding.userContainer.setVisibility(View.VISIBLE);
                if (model.kind == UserInfoAsync.Info.Google) {
                    binding.sourceName.setText(mContext.getString(R.string.google_drive));
                } else if (model.kind == UserInfoAsync.Info.Dropbox) {
                    binding.sourceName.setText(mContext.getString(R.string.dropbox));
                }
            }
            String name = model.name;
            if (!TextUtils.isEmpty(name)) {
                binding.cloudUser.setText(name);
            }
            String photoLink = model.photo;
            if (photoLink != null) {
                loadImage(photoLink, binding.userPhoto);
            }
            showQuota(binding, model);
            binding.cloudCount.setText(String.valueOf(model.count));
        }
    }

    private void showPopup(UserInfoAsync.Info kind, View view) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.delete_all:
                    if (mCallback != null) mCallback.onItemClick(kind);
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showQuota(BackupItemLayoutBinding binding, UserItem model) {
        long quota = model.quota;
        if (quota != 0) {
            final long availQ = quota - (model.used);
            final float free = (int) ((availQ * 100.0f) / quota);
            final float used = (int) ((model.used * 100.0f) / quota);
            binding.usedSizeGraph.removeSlices();
            PieSlice slice = new PieSlice();
            final String usTitle = String.format(mContext.getString(R.string.used_x), String.valueOf(used));
            slice.setTitle(usTitle);
            slice.setColor(ViewUtils.getColor(mContext, R.color.redPrimary));
            slice.setValue(used);
            binding.usedSizeGraph.addSlice(slice);
            slice = new PieSlice();
            final String avTitle = String.format(mContext.getString(R.string.available_x), String.valueOf(free));
            slice.setTitle(avTitle);
            slice.setColor(ViewUtils.getColor(mContext, R.color.greenPrimary));
            slice.setValue(free);
            binding.usedSizeGraph.addSlice(slice);
            binding.usedSpace.setText(String.format(mContext.getString(R.string.used_x),
                    MemoryUtil.humanReadableByte(model.used, false)));
            binding.freeSpace.setText(String.format(mContext.getString(R.string.available_x),
                    MemoryUtil.humanReadableByte(availQ, false)));
        }
    }

    private void loadImage(final String photoLink, ImageView userPhoto) {
        File dir = MemoryUtil.getImagesDir();
        File image = new File(dir, FILE_NAME);
        if (image.exists()) {
            Picasso.with(mContext).load(image).transform(new CropCircleTransformation()).into(userPhoto);
            userPhoto.setVisibility(View.VISIBLE);
        } else {
            Picasso.with(mContext).load(photoLink).transform(new CropCircleTransformation()).into(userPhoto);
            userPhoto.setVisibility(View.VISIBLE);
            saveImageFile(photoLink);
        }
    }

    private void saveImageFile(String photoLink) {
        new Thread(() -> {
            try {
                Bitmap bitmap = Picasso.with(mContext)
                        .load(photoLink)
                        .get();
                try {
                    File dir1 = MemoryUtil.getImagesDir();
                    File image1 = new File(dir1, FILE_NAME);
                    if (image1.createNewFile()) {
                        FileOutputStream stream = new FileOutputStream(image1);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private BackupItemLayoutBinding getView() {
        return BackupItemLayoutBinding.inflate(LayoutInflater.from(mContext));
    }

    public interface ActionCallback {
        void onItemClick(UserInfoAsync.Info info);
    }
}
