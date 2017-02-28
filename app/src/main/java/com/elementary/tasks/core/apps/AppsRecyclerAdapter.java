package com.elementary.tasks.core.apps;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.core.adapter.FilterableAdapter;
import com.elementary.tasks.core.file_explorer.RecyclerClickListener;
import com.elementary.tasks.databinding.ApplicationListItemBinding;

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

public class AppsRecyclerAdapter extends FilterableAdapter<ApplicationItem, String, AppsRecyclerAdapter.ApplicationViewHolder> {

    private Context mContext;
    private RecyclerClickListener mListener;

    AppsRecyclerAdapter(Context context, List<ApplicationItem> dataItemList, RecyclerClickListener listener, Filter<ApplicationItem, String> filter) {
        super(dataItemList, filter);
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
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
