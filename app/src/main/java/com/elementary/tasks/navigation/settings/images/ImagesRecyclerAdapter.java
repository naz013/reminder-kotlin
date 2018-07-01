package com.elementary.tasks.navigation.settings.images;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elementary.tasks.core.data.models.MainImage;
import com.elementary.tasks.core.network.RetrofitBuilder;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemPhotoBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ImagesRecyclerAdapter extends RecyclerView.Adapter<ImagesRecyclerAdapter.PhotoViewHolder> {

    private static final String TAG = "ImagesRecyclerAdapter";

    private Context mContext;
    private List<MainImage> mDataList;
    private int prevSelected = -1;
    private SelectListener mListener;
    private Prefs mPrefs;

    ImagesRecyclerAdapter(Context context, List<MainImage> dataItemList, SelectListener listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
        this.mPrefs = Prefs.getInstance(context);
    }

    void deselectLast() {
        if (prevSelected != -1) {
            prevSelected = -1;
            notifyItemChanged(prevSelected);
        }
    }

    void setPrevSelected(int prevSelected) {
        this.prevSelected = prevSelected;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PhotoViewHolder(ListItemPhotoBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        MainImage item = mDataList.get(position);
        holder.binding.setItem(item);
        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) holder.binding.card.getLayoutParams();
        if (position < 3) {
            params.topMargin = MeasureUtils.dp2px(mContext, 56);
        } else {
            params.topMargin = 0;
        }
        holder.binding.card.setLayoutParams(params);
        if (prevSelected == position) {
            holder.binding.setSelected(true);
        } else {
            holder.binding.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ListItemPhotoBinding binding;
        PhotoViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.container.setOnClickListener(view -> performClick(getAdapterPosition()));
            binding.container.setOnLongClickListener(view -> {
                if (mListener != null) {
                    mListener.onItemLongClicked(getAdapterPosition(), view);
                }
                return true;
            });
        }
    }

    void addItems(List<MainImage> list) {
        mDataList.addAll(list);
        notifyItemInserted(getItemCount() - list.size());
    }

    private void performClick(int position) {
        if (position == prevSelected) {
            prevSelected = -1;
            mPrefs.setImageId(-1);
            mPrefs.setImagePath("");
            notifyItemChanged(prevSelected);
            if (mListener != null) mListener.onImageSelected(false);
        } else {
            if (prevSelected != -1) {
                if (prevSelected >= getItemCount() && mListener != null) {
                    mListener.deselectOverItem(prevSelected);
                } else {
                    notifyItemChanged(prevSelected);
                }
            }
            prevSelected = position;
            MainImage item = mDataList.get(position);
            mPrefs.setImageId(position);
            mPrefs.setImagePath(RetrofitBuilder.getImageLink(item.getId()));
            notifyItemChanged(position);
            if (mListener != null) mListener.onImageSelected(true);
        }
    }

    @BindingAdapter({"loadPhoto"})
    public static void loadPhoto(ImageView imageView, long id) {
        boolean isDark = ThemeUtil.getInstance(imageView.getContext()).isDark();
        String url = RetrofitBuilder.getImageLink(id, 800, 480);
        Glide.with(imageView).load(url).into(imageView);
    }
}
