package com.elementary.tasks.places.google

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemSimpleTextAdvancedBinding

import java.util.ArrayList

import androidx.recyclerview.widget.RecyclerView

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GooglePlacesAdapter(context: Context, array: List<GooglePlaceItem>) : RecyclerView.Adapter<GooglePlacesAdapter.ViewHolder>() {

    private val array = ArrayList<GooglePlaceItem>()
    private var mEventListener: SimpleListener? = null
    private val isDark: Boolean

    private val last: Int
        get() = itemCount - 1

    init {
        this.array.addAll(array)
        isDark = ThemeUtil.getInstance(context).isDark
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var binding: ListItemSimpleTextAdvancedBinding? = null

        init {
            binding = DataBindingUtil.bind(v)
            v.setOnClickListener { view ->
                if (itemCount > 1 && adapterPosition == last) {
                    for (item in array) item.isSelected = !item.isSelected
                    notifyDataSetChanged()
                } else {
                    if (mEventListener != null) {
                        mEventListener!!.onItemClicked(adapterPosition, view)
                    }
                }
            }
            binding!!.placeCheck.setOnClickListener { v1 ->
                val item = array[adapterPosition]
                item.isSelected = !item.isSelected
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemSimpleTextAdvancedBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = array[position]
        holder.binding!!.item = item
        holder.binding!!.placeIcon.setImageResource(getIcon(item.types))
        if (itemCount > 1 && position == last) {
            holder.binding!!.placeCheck.visibility = View.GONE
            holder.binding!!.placeIcon.visibility = View.GONE
            holder.binding!!.text2.text = ""
        } else {
            holder.binding!!.placeCheck.visibility = View.VISIBLE
            holder.binding!!.placeIcon.visibility = View.VISIBLE
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return array.size
    }

    private fun getIcon(tags: List<String>?): Int {
        if (tags == null) {
            return if (isDark)
                R.drawable.ic_map_marker_white
            else
                R.drawable.ic_map_marker
        }
        val sb = StringBuilder()
        for (t in tags) sb.append(t).append(",")
        val tag = sb.toString()
        return if (tag.contains("florist")) {
            if (isDark)
                R.drawable.ic_local_florist_white_24dp
            else
                R.drawable.ic_local_florist_black_24dp
        } else if (tag.contains("school")) {
            if (isDark)
                R.drawable.ic_school_white_24dp
            else
                R.drawable.ic_school_black_24dp
        } else if (tag.contains("cafe")) {
            if (isDark)
                R.drawable.ic_local_cafe_white_24dp
            else
                R.drawable.ic_local_cafe_black_24dp
        } else if (tag.contains("restaurant")) {
            if (isDark)
                R.drawable.ic_restaurant_menu_white_24dp
            else
                R.drawable.ic_restaurant_menu_black_24dp
        } else if (tag.contains("bus_station")) {
            if (isDark)
                R.drawable.ic_directions_bus_white_24dp
            else
                R.drawable.ic_directions_bus_black_24dp
        } else if (tag.contains("subway_station")) {
            if (isDark)
                R.drawable.ic_directions_subway_white_24dp
            else
                R.drawable.ic_directions_subway_black_24dp
        } else if (tag.contains("train_station")) {
            if (isDark)
                R.drawable.ic_directions_subway_white_24dp
            else
                R.drawable.ic_directions_subway_black_24dp
        } else if (tag.contains("bicycle_store")) {
            if (isDark)
                R.drawable.ic_directions_bike_white_24dp
            else
                R.drawable.ic_directions_bike_black_24dp
        } else if (tag.contains("car_repair") || tag.contains("car_rental") || tag.contains("car_dealer")) {
            if (isDark)
                R.drawable.ic_directions_car_white_24dp
            else
                R.drawable.ic_directions_car_black_24dp
        } else if (tag.contains("taxi") || tag.contains("taxi_stand")) {
            if (isDark)
                R.drawable.ic_local_taxi_white_24dp
            else
                R.drawable.ic_local_taxi_black_24dp
        } else if (tag.contains("atm")) {
            if (isDark)
                R.drawable.ic_local_atm_white_24dp
            else
                R.drawable.ic_local_atm_black_24dp
        } else if (tag.contains("bar")) {
            if (isDark)
                R.drawable.ic_local_bar_white_24dp
            else
                R.drawable.ic_local_bar_black_24dp
        } else if (tag.contains("airport")) {
            if (isDark)
                R.drawable.ic_local_airport_white_24dp
            else
                R.drawable.ic_local_airport_black_24dp
        } else if (tag.contains("car_wash")) {
            if (isDark)
                R.drawable.ic_local_car_wash_white_24dp
            else
                R.drawable.ic_local_car_wash_black_24dp
        } else if (tag.contains("convenience_store")) {
            if (isDark)
                R.drawable.ic_local_convenience_store_white_24dp
            else
                R.drawable.ic_local_convenience_store_black_24dp
        } else if (tag.contains("gas_station")) {
            if (isDark)
                R.drawable.ic_local_gas_station_white_24dp
            else
                R.drawable.ic_local_gas_station_black_24dp
        } else if (tag.contains("hospital") || tag.contains("doctor") ||
                tag.contains("physiotherapist") || tag.contains("health")) {
            if (isDark)
                R.drawable.ic_local_hospital_white_24dp
            else
                R.drawable.ic_local_hospital_black_24dp
        } else if (tag.contains("grocery_or_supermarket")) {
            if (isDark)
                R.drawable.ic_local_grocery_store_white_24dp
            else
                R.drawable.ic_local_grocery_store_black_24dp
        } else if (tag.contains("night_club") || tag.contains("liquor_store")) {
            if (isDark)
                R.drawable.ic_local_drink_white_24dp
            else
                R.drawable.ic_local_drink_black_24dp
        } else if (tag.contains("meal_takeaway")) {
            if (isDark)
                R.drawable.ic_local_pizza_white_24dp
            else
                R.drawable.ic_local_pizza_black_24dp
        } else if (tag.contains("pharmacy")) {
            if (isDark)
                R.drawable.ic_local_pharmacy_white_24dp
            else
                R.drawable.ic_local_pharmacy_black_24dp
        } else if (tag.contains("meal_delivery") || tag.contains("moving_company")) {
            if (isDark)
                R.drawable.ic_local_shipping_white_24dp
            else
                R.drawable.ic_local_shipping_black_24dp
        } else if (tag.contains("parking")) {
            if (isDark)
                R.drawable.ic_local_parking_white_24dp
            else
                R.drawable.ic_local_parking_black_24dp
        } else if (tag.contains("electronics_store")) {
            if (isDark)
                R.drawable.ic_local_printshop_white_24dp
            else
                R.drawable.ic_local_printshop_black_24dp
        } else if (tag.contains("laundry")) {
            if (isDark)
                R.drawable.ic_local_laundry_service_white_24dp
            else
                R.drawable.ic_local_laundry_service_black_24dp
        } else if (tag.contains("book_store") || tag.contains("library")) {
            if (isDark)
                R.drawable.ic_local_library_white_24dp
            else
                R.drawable.ic_local_library_black_24dp
        } else if (tag.contains("post_office")) {
            if (isDark)
                R.drawable.ic_local_post_office_white_24dp
            else
                R.drawable.ic_local_post_office_black_24dp
        } else if (tag.contains("movie_rental") || tag.contains("movie_theater")) {
            if (isDark)
                R.drawable.ic_local_movies_white_24dp
            else
                R.drawable.ic_local_movies_black_24dp
        } else if (tag.contains("real_estate_agency") || tag.contains("establishment")) {
            if (isDark)
                R.drawable.ic_local_hotel_white_24dp
            else
                R.drawable.ic_local_hotel_black_24dp
        } else if (tag.contains("clothing_store") || tag.contains("home_goods_store")
                || tag.contains("shopping_mall") || tag.contains("shoe_store")) {
            if (isDark)
                R.drawable.ic_local_mall_white_24dp
            else
                R.drawable.ic_local_mall_black_24dp
        } else if (tag.contains("food")) {
            if (isDark)
                R.drawable.ic_restaurant_menu_white_24dp
            else
                R.drawable.ic_restaurant_menu_black_24dp
        } else {
            if (isDark)
                R.drawable.ic_map_marker_white
            else
                R.drawable.ic_map_marker
        }
    }

    fun setEventListener(eventListener: SimpleListener) {
        mEventListener = eventListener
    }
}
