package com.elementary.tasks.places.google;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.SimpleTextItemAdvancedBinding;

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

public class GooglePlacesAdapter extends RecyclerView.Adapter<GooglePlacesAdapter.ViewHolder> {

    private List<GooglePlaceItem> array = new ArrayList<>();
    private SimpleListener mEventListener;
    private boolean isDark;

    public GooglePlacesAdapter(final Context context, List<GooglePlaceItem> array) {
        this.array.addAll(array);
        isDark = ThemeUtil.getInstance(context).isDark();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public SimpleTextItemAdvancedBinding binding;

        public ViewHolder(final View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            v.setOnClickListener(view -> {
                if (getItemCount() > 1 && getAdapterPosition() == getLast()) {
                    for (GooglePlaceItem item : array) item.setSelected(!item.isSelected());
                    notifyDataSetChanged();
                } else {
                    if (mEventListener != null) {
                        mEventListener.onItemClicked(getAdapterPosition(), view);
                    }
                }
            });
            binding.placeCheck.setOnClickListener(v1 -> {
                GooglePlaceItem item = array.get(getAdapterPosition());
                item.setSelected(!item.isSelected());
                notifyItemChanged(getAdapterPosition());
            });
        }
    }

    private int getLast() {
        return getItemCount() - 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(SimpleTextItemAdvancedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GooglePlaceItem item = array.get(position);
        holder.binding.setItem(item);
        holder.binding.placeIcon.setImageResource(getIcon(item.getTypes()));
        if (getItemCount() > 1 && position == getLast()) {
            holder.binding.placeCheck.setVisibility(View.GONE);
            holder.binding.placeIcon.setVisibility(View.GONE);
            holder.binding.text2.setText("");
        } else {
            holder.binding.placeCheck.setVisibility(View.VISIBLE);
            holder.binding.placeIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    private int getIcon(List<String> tags) {
        if (tags == null) {
            if (isDark) return R.drawable.ic_map_marker_white;
            else return R.drawable.ic_map_marker;
        }
        StringBuilder sb = new StringBuilder();
        for (String t : tags) sb.append(t).append(",");
        String tag = sb.toString();
        if (tag.contains("florist")) {
            if (isDark) return R.drawable.ic_local_florist_white_24dp;
            else return R.drawable.ic_local_florist_black_24dp;
        } else if (tag.contains("school")) {
            if (isDark) return R.drawable.ic_school_white_24dp;
            else return R.drawable.ic_school_black_24dp;
        } else if (tag.contains("cafe")) {
            if (isDark) return R.drawable.ic_local_cafe_white_24dp;
            else return R.drawable.ic_local_cafe_black_24dp;
        } else if (tag.contains("restaurant")) {
            if (isDark) return R.drawable.ic_restaurant_menu_white_24dp;
            else return R.drawable.ic_restaurant_menu_black_24dp;
        } else if (tag.contains("bus_station")) {
            if (isDark) return R.drawable.ic_directions_bus_white_24dp;
            else return R.drawable.ic_directions_bus_black_24dp;
        } else if (tag.contains("subway_station")) {
            if (isDark) return R.drawable.ic_directions_subway_white_24dp;
            else return R.drawable.ic_directions_subway_black_24dp;
        } else if (tag.contains("train_station")) {
            if (isDark) return R.drawable.ic_directions_subway_white_24dp;
            else return R.drawable.ic_directions_subway_black_24dp;
        } else if (tag.contains("bicycle_store")) {
            if (isDark) return R.drawable.ic_directions_bike_white_24dp;
            else return R.drawable.ic_directions_bike_black_24dp;
        } else if (tag.contains("car_repair") || tag.contains("car_rental") || tag.contains("car_dealer")) {
            if (isDark) return R.drawable.ic_directions_car_white_24dp;
            else return R.drawable.ic_directions_car_black_24dp;
        } else if (tag.contains("taxi") || tag.contains("taxi_stand")) {
            if (isDark) return R.drawable.ic_local_taxi_white_24dp;
            else return R.drawable.ic_local_taxi_black_24dp;
        } else if (tag.contains("atm")) {
            if (isDark) return R.drawable.ic_local_atm_white_24dp;
            else return R.drawable.ic_local_atm_black_24dp;
        } else if (tag.contains("bar")) {
            if (isDark) return R.drawable.ic_local_bar_white_24dp;
            else return R.drawable.ic_local_bar_black_24dp;
        } else if (tag.contains("airport")) {
            if (isDark) return R.drawable.ic_local_airport_white_24dp;
            else return R.drawable.ic_local_airport_black_24dp;
        } else if (tag.contains("car_wash")) {
            if (isDark) return R.drawable.ic_local_car_wash_white_24dp;
            else return R.drawable.ic_local_car_wash_black_24dp;
        } else if (tag.contains("convenience_store")) {
            if (isDark) return R.drawable.ic_local_convenience_store_white_24dp;
            else return R.drawable.ic_local_convenience_store_black_24dp;
        } else if (tag.contains("gas_station")) {
            if (isDark) return R.drawable.ic_local_gas_station_white_24dp;
            else return R.drawable.ic_local_gas_station_black_24dp;
        } else if (tag.contains("hospital") || tag.contains("doctor") ||
                tag.contains("physiotherapist") || tag.contains("health")) {
            if (isDark) return R.drawable.ic_local_hospital_white_24dp;
            else return R.drawable.ic_local_hospital_black_24dp;
        } else if (tag.contains("grocery_or_supermarket")) {
            if (isDark) return R.drawable.ic_local_grocery_store_white_24dp;
            else return R.drawable.ic_local_grocery_store_black_24dp;
        } else if (tag.contains("night_club") || tag.contains("liquor_store")) {
            if (isDark) return R.drawable.ic_local_drink_white_24dp;
            else return R.drawable.ic_local_drink_black_24dp;
        } else if (tag.contains("meal_takeaway")) {
            if (isDark) return R.drawable.ic_local_pizza_white_24dp;
            else return R.drawable.ic_local_pizza_black_24dp;
        } else if (tag.contains("pharmacy")) {
            if (isDark) return R.drawable.ic_local_pharmacy_white_24dp;
            else return R.drawable.ic_local_pharmacy_black_24dp;
        } else if (tag.contains("meal_delivery") || tag.contains("moving_company")) {
            if (isDark) return R.drawable.ic_local_shipping_white_24dp;
            else return R.drawable.ic_local_shipping_black_24dp;
        } else if (tag.contains("parking")) {
            if (isDark) return R.drawable.ic_local_parking_white_24dp;
            else return R.drawable.ic_local_parking_black_24dp;
        } else if (tag.contains("electronics_store")) {
            if (isDark) return R.drawable.ic_local_printshop_white_24dp;
            else return R.drawable.ic_local_printshop_black_24dp;
        } else if (tag.contains("laundry")) {
            if (isDark) return R.drawable.ic_local_laundry_service_white_24dp;
            else return R.drawable.ic_local_laundry_service_black_24dp;
        } else if (tag.contains("book_store") || tag.contains("library")) {
            if (isDark) return R.drawable.ic_local_library_white_24dp;
            else return R.drawable.ic_local_library_black_24dp;
        } else if (tag.contains("post_office")) {
            if (isDark) return R.drawable.ic_local_post_office_white_24dp;
            else return R.drawable.ic_local_post_office_black_24dp;
        } else if (tag.contains("movie_rental") || tag.contains("movie_theater")) {
            if (isDark) return R.drawable.ic_local_movies_white_24dp;
            else return R.drawable.ic_local_movies_black_24dp;
        } else if (tag.contains("real_estate_agency") || tag.contains("establishment")) {
            if (isDark) return R.drawable.ic_local_hotel_white_24dp;
            else return R.drawable.ic_local_hotel_black_24dp;
        } else if (tag.contains("clothing_store") || tag.contains("home_goods_store")
                || tag.contains("shopping_mall") || tag.contains("shoe_store")) {
            if (isDark) return R.drawable.ic_local_mall_white_24dp;
            else return R.drawable.ic_local_mall_black_24dp;
        } else if (tag.contains("food")) {
            if (isDark) return R.drawable.ic_restaurant_menu_white_24dp;
            else return R.drawable.ic_restaurant_menu_black_24dp;
        } else {
            if (isDark) return R.drawable.ic_map_marker_white;
            else return R.drawable.ic_map_marker;
        }
    }

    public void setEventListener(final SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}
