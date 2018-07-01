package com.elementary.tasks.places.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.interfaces.ActionsListener;
import com.elementary.tasks.core.utils.ListActions;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemPlaceBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
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
public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {

    private List<Place> mData = new ArrayList<>();
    @Nullable
    private ActionsListener<Place> actionsListener;

    public void setActionsListener(@Nullable ActionsListener<Place> actionsListener) {
        this.actionsListener = actionsListener;
    }

    @Nullable
    public ActionsListener<Place> getActionsListener() {
        return actionsListener;
    }

    public void setData(List<Place> list) {
        this.mData.clear();
        this.mData.addAll(list);
        notifyDataSetChanged();
    }

    public List<Place> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListItemPlaceBinding binding;

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

    public Place getItem(int position) {
        return mData.get(position);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemPlaceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.binding.setItem(getItem(position));
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(ImageView view, int color) {
        view.setImageResource(ThemeUtil.getInstance(view.getContext()).getMarkerStyle(color));
    }
}
