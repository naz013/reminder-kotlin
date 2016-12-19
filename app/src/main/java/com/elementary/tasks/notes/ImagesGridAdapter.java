package com.elementary.tasks.notes;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.NoteImageListItemBinding;

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

public class ImagesGridAdapter extends RecyclerView.Adapter<ImagesGridAdapter.PhotoViewHolder> {

    private Context mContext;
    private List<NoteImage> mDataList;

    ImagesGridAdapter(Context context) {
        this.mContext = context;
        this.mDataList = new ArrayList<>();
    }

    List<NoteImage> getImages() {
        return mDataList;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new PhotoViewHolder(NoteImageListItemBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        NoteImage item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        NoteImageListItemBinding binding;
        PhotoViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.photoView.setOnClickListener(view -> performClick(getAdapterPosition()));
        }
    }

    void setImages(List<NoteImage> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    void addNextImages(List<NoteImage> list) {
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    void addImage(NoteImage image) {
        mDataList.add(image);
        notifyDataSetChanged();
    }

    private void performClick(int position) {
        NoteItem item = new NoteItem();
        item.setImages(mDataList);
        RealmDb.getInstance().saveObject(item);
        mContext.startActivity(new Intent(mContext, ImagePreviewActivity.class)
                .putExtra(Constants.INTENT_ID, item.getKey())
                .putExtra(Constants.INTENT_POSITION, position));
    }

    @BindingAdapter("loadImage")
    public static void loadImage(ImageView imageView, NoteImage image) {
        Glide.with(imageView.getContext()).load(image.getImage()).crossFade().into(imageView);
    }
}
