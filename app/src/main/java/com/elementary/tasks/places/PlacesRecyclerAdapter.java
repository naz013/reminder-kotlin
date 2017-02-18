package com.elementary.tasks.places;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.core.adapter.FilterableAdapter;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.PlaceListItemBinding;

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

public class PlacesRecyclerAdapter extends FilterableAdapter<PlaceItem, String, PlacesRecyclerAdapter.ViewHolder> {

    private SimpleListener mEventListener;
    private Context mContext;

    public PlacesRecyclerAdapter(Context context, List<PlaceItem> list, SimpleListener listener, Filter<PlaceItem, String> filter) {
        super(list, filter);
        this.mContext = context;
        this.mEventListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        PlaceListItemBinding binding;
        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            v.setOnClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemClicked(getAdapterPosition(), view);
                }
            });
            v.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), view);
                }
                return true;
            });
        }
    }

    public void deleteItem(int position) {
        RealmDb.getInstance().deletePlace(getUsedData().remove(position));
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, getUsedData().size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PlaceListItemBinding binding = PlaceListItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        PlaceItem item = getUsedData().get(position);
        holder.binding.setItem(item);
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(ImageView view, int color) {
        view.setImageResource(ThemeUtil.getInstance(view.getContext()).getMarkerStyle(color));
    }
}
