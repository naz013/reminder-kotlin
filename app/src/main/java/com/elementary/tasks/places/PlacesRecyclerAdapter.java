package com.elementary.tasks.places;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.PlaceListItemBinding;

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

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {

    private List<PlaceItem> mDataList = new ArrayList<>();
    private SimpleListener mEventListener;
    private Context mContext;

    public PlacesRecyclerAdapter(Context context, List<PlaceItem> list, SimpleListener listener) {
        this.mDataList = new ArrayList<>(list);
        this.mContext = context;
        this.mEventListener = listener;
    }

    public List<PlaceItem> getData() {
        return mDataList;
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
        RealmDb.getInstance().deletePlace(mDataList.remove(position));
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PlaceListItemBinding binding = PlaceListItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        PlaceItem item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    public PlaceItem getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(ImageView view, int color) {
        view.setImageResource(new ThemeUtil(view.getContext()).getMarkerStyle(color));
    }
}
