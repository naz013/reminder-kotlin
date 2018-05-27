package com.elementary.tasks.places;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.interfaces.ActionsListener;
import com.elementary.tasks.core.utils.ListActions;
import com.elementary.tasks.databinding.LocationListItemBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
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
public class LocationPlacesAdapter extends RecyclerView.Adapter<LocationPlacesAdapter.ViewHolder> {

    private List<Reminder> mDataList = new ArrayList<>();
    @Nullable
    private ActionsListener<Reminder> actionsListener;

    public void setActionsListener(@Nullable ActionsListener<Reminder> actionsListener) {
        this.actionsListener = actionsListener;
    }

    @Nullable
    public ActionsListener<Reminder> getActionsListener() {
        return actionsListener;
    }

    public void setData(List<Reminder> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LocationListItemBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            v.setOnClickListener(view -> {
                if (getActionsListener() != null) {
                    getActionsListener().onAction(view, getAdapterPosition(), getItem(getAdapterPosition()), ListActions.OPEN);
                }
            });
            v.setOnLongClickListener(view -> {
                if (getActionsListener() != null) {
                    getActionsListener().onAction(view, getAdapterPosition(), getItem(getAdapterPosition()), ListActions.MORE);
                }
                return true;
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocationListItemBinding binding = LocationListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Reminder item = mDataList.get(position);
        Place place = item.getPlaces().get(0);
        String name = place.getName();
        if (item.getPlaces().size() > 1) {
            name = item.getSummary() + " (" + item.getPlaces().size() + ")";
        }
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
