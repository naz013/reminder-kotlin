package com.elementary.tasks.places;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.databinding.LocationListItemBinding;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;

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

public class LocationPlacesAdapter extends RecyclerView.Adapter<LocationPlacesAdapter.ViewHolder> {

    private List<Reminder> mDataList = new ArrayList<>();
    private SimpleListener mEventListener;
    private Context mContext;

    public LocationPlacesAdapter(Context context, List<Reminder> list, SimpleListener listener) {
        this.mDataList = new ArrayList<>(list);
        this.mContext = context;
        this.mEventListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LocationListItemBinding binding;

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocationListItemBinding binding = LocationListItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Reminder item = mDataList.get(position);
        Place place = item.getPlaces().get(0);
        String name = place.getName();
        if (item.getPlaces().size() > 1) name = item.getSummary() + " (" + item.getPlaces().size() + ")";
        holder.binding.setItem(place);
        holder.binding.setName(name);
    }

    public Reminder getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
