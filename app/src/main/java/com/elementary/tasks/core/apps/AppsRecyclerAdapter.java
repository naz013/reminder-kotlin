package com.elementary.tasks.core.apps;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.core.file_explorer.RecyclerClickListener;
import com.elementary.tasks.databinding.ApplicationListItemBinding;

import java.util.ArrayList;
import java.util.List;

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
public class AppsRecyclerAdapter extends RecyclerView.Adapter<AppsRecyclerAdapter.ApplicationViewHolder> {

    private RecyclerClickListener mListener;
    private List<ApplicationItem> mData = new ArrayList<>();

    AppsRecyclerAdapter(RecyclerClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<ApplicationItem> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    public List<ApplicationItem> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public ApplicationItem getItem(int position) {
        return mData.get(position);
    }

    public void removeItem(int position) {
        if (position < mData.size()) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, mData.size());
        }
    }

    @Override
    public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ApplicationViewHolder(ApplicationListItemBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ApplicationViewHolder holder, int position) {
        ApplicationItem item = getItem(position);
        holder.binding.setItem(item);
    }

    class ApplicationViewHolder extends RecyclerView.ViewHolder {
        ApplicationListItemBinding binding;

        ApplicationViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.setClick(view -> {
                if (mListener != null) {
                    mListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    @BindingAdapter("loadImage")
    public static void loadImage(ImageView imageView, Drawable v) {
        imageView.setImageDrawable(v);
    }
}
