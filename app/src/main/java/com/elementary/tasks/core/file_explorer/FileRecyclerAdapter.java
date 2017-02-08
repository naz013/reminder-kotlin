package com.elementary.tasks.core.file_explorer;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemFileLayoutBinding;

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

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.ContactViewHolder> {

    private static final String TAG = "FileRecyclerAdapter";

    private Context mContext;
    private List<FileDataItem> mDataList;

    private RecyclerClickListener mListener;
    private FilterCallback mCallback;

    public FileRecyclerAdapter(Context context, List<FileDataItem> dataItemList, RecyclerClickListener listener, FilterCallback callback) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
        this.mCallback = callback;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ContactViewHolder(ListItemFileLayoutBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        FileDataItem item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        ListItemFileLayoutBinding binding;
        public ContactViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.setClick(view -> {
                if (mListener != null) {
                    mListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public void filter(String q, List<FileDataItem> list) {
        List<FileDataItem> res = filter(list, q);
        animateTo(res);
        if (mCallback != null) mCallback.filter(res.size());
    }

    private List<FileDataItem> filter(List<FileDataItem> mData, String q) {
        q = q.toLowerCase();
        if (mData == null) mData = new ArrayList<>();
        List<FileDataItem> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<FileDataItem> getFiltered(List<FileDataItem> models, String query) {
        List<FileDataItem> list = new ArrayList<>();
        for (FileDataItem model : models) {
            final String text = model.getFileName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    public FileDataItem getItem(int position) {
        return mDataList.get(position);
    }

    public FileDataItem removeItem(int position) {
        final FileDataItem model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, FileDataItem model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final FileDataItem model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<FileDataItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<FileDataItem> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final FileDataItem model = mDataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<FileDataItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final FileDataItem model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<FileDataItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final FileDataItem model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @BindingAdapter("loadImage")
    public static void loadImage(ImageView imageView, String v) {
        boolean isDark = new ThemeUtil(imageView.getContext()).isDark();
        imageView.setImageResource(getFileIcon(v, isDark));
    }

    private static int getFileIcon(String file, boolean isDark){
        Log.d(TAG, "getFileIcon: " + file);
        if (isMelody(file)) {
            Log.d(TAG, "getFileIcon: isMelody");
            return isDark ? R.drawable.ic_music_note_white_24dp : R.drawable.ic_music_note_black_24dp;
        } else if (isPicture(file)) {
            Log.d(TAG, "getFileIcon: isPicture");
            return isDark ? R.drawable.ic_image_white_24dp : R.drawable.ic_image_black_24dp;
        } else if (isMovie(file)) {
            Log.d(TAG, "getFileIcon: isMovie");
            return isDark ? R.drawable.ic_movie_white_24dp : R.drawable.ic_movie_black_24dp;
        } else if (isGif(file)) {
            Log.d(TAG, "getFileIcon: isGif");
            return isDark ? R.drawable.ic_gif_white_24dp : R.drawable.ic_gif_black_24dp;
        } else if (isArchive(file)) {
            Log.d(TAG, "getFileIcon: isArchive");
            return isDark ? R.drawable.ic_storage_white_24dp : R.drawable.ic_storage_black_24dp;
        } else if (isAndroid(file)) {
            Log.d(TAG, "getFileIcon: isAndroid");
            return isDark ? R.drawable.ic_android_white_24dp : R.drawable.ic_android_black_24dp;
        } else if (!file.contains(".")) {
            Log.d(TAG, "getFileIcon: folder");
            return isDark ? R.drawable.ic_folder_white_24dp : R.drawable.ic_folder_black_24dp;
        } else {
            Log.d(TAG, "getFileIcon: else");
            return isDark ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_insert_drive_file_black_24dp;
        }
    }

    private static boolean isPicture(String file){
        return file.contains(".jpg") || file.contains(".jpeg") || file.contains(".png");
    }

    private static boolean isArchive(String file){
        return file.contains(".zip") || file.contains(".rar") || file.contains(".tar.gz");
    }

    private static boolean isMovie(String file){
        return file.contains(".mov") || file.contains(".3gp") || file.contains(".avi") ||
                file.contains(".mkv") || file.contains(".vob") || file.contains(".divx") ||
                file.contains(".mp4") || file.contains(".flv");
    }

    private static boolean isGif(String file){
        return file.contains(".gif");
    }

    private static boolean isAndroid(String file){
        return file.contains(".apk");
    }

    private static boolean isMelody(String file){
        return file.contains(".mp3") || file.contains(".ogg") || file.contains(".m4a") || file.contains(".flac");
    }
}
