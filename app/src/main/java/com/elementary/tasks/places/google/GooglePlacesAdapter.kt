package com.elementary.tasks.places.google

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.databinding.ListItemGooglePlaceBinding

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
class GooglePlacesAdapter : RecyclerView.Adapter<GooglePlacesAdapter.ViewHolder>() {

    private val array = mutableListOf<GooglePlaceItem>()
    private var mEventListener: SimpleListener? = null

    private val last: Int
        get() = itemCount - 1

    fun setPlaces(list: List<GooglePlaceItem>) {
        this.array.clear()
        this.array.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(parent: ViewGroup) : HolderBinding<ListItemGooglePlaceBinding>(parent, R.layout.list_item_google_place) {
        fun bind(googlePlaceItem: GooglePlaceItem) {
            binding.text1.text = googlePlaceItem.name
            binding.text2.text = googlePlaceItem.address
            binding.placeIcon.setImageResource(getIcon(googlePlaceItem.types))
            binding.placeCheck.isChecked = googlePlaceItem.isSelected
            if (itemCount > 1 && adapterPosition == last) {
                binding.placeCheck.visibility = View.GONE
                binding.placeIcon.visibility = View.GONE
                binding.text2.text = ""
            } else {
                binding.placeCheck.visibility = View.VISIBLE
                binding.placeIcon.visibility = View.VISIBLE
            }
        }

        init {
            binding.listItem.setOnClickListener { view ->
                if (itemCount > 1 && adapterPosition == last) {
                    array.forEach {
                        it.isSelected = true
                    }
                    notifyDataSetChanged()
                } else {
                    mEventListener?.onItemClicked(adapterPosition, view)
                }
            }
            binding.placeCheck.setOnClickListener {
                array[adapterPosition].isSelected = !array[adapterPosition].isSelected
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(array[position])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return array.size
    }

    private fun getIcon(tags: List<String>?): Int {
        if (tags == null) {
            return R.drawable.ic_twotone_place_24px
        }
        val sb = StringBuilder()
        for (t in tags) sb.append(t).append(",")
        val tag = sb.toString()
        return if (tag.contains("florist")) {
            R.drawable.ic_twotone_local_florist_24px
        } else if (tag.contains("school")) {
            R.drawable.ic_twotone_school_24px
        } else if (tag.contains("cafe")) {
            R.drawable.ic_twotone_local_cafe_24px
        } else if (tag.contains("restaurant")) {
            R.drawable.ic_twotone_restaurant_24px
        } else if (tag.contains("bus_station")) {
            R.drawable.ic_twotone_directions_bus_24px
        } else if (tag.contains("subway_station") || tag.contains("train_station")) {
            R.drawable.ic_twotone_directions_subway_24px
        } else if (tag.contains("bicycle_store")) {
            R.drawable.ic_twotone_directions_bike_24px
        } else if (tag.contains("car_repair") || tag.contains("car_rental") || tag.contains("car_dealer")) {
            R.drawable.ic_twotone_directions_car_24px
        } else if (tag.contains("taxi") || tag.contains("taxi_stand")) {
            R.drawable.ic_twotone_local_taxi_24px
        } else if (tag.contains("atm")) {
            R.drawable.ic_twotone_local_atm_24px
        } else if (tag.contains("bar")) {
            R.drawable.ic_twotone_local_bar_24px
        } else if (tag.contains("airport")) {
            R.drawable.ic_twotone_local_airport_24px
        } else if (tag.contains("car_wash")) {
            R.drawable.ic_twotone_local_car_wash_24px
        } else if (tag.contains("convenience_store")) {
            R.drawable.ic_twotone_local_convenience_store_24px
        } else if (tag.contains("gas_station")) {
            R.drawable.ic_twotone_local_gas_station_24px
        } else if (tag.contains("hospital") || tag.contains("doctor") ||
                tag.contains("physiotherapist") || tag.contains("health")) {
            R.drawable.ic_twotone_local_hospital_24px
        } else if (tag.contains("grocery_or_supermarket")) {
            R.drawable.ic_twotone_local_grocery_store_24px
        } else if (tag.contains("night_club") || tag.contains("liquor_store")) {
            R.drawable.ic_twotone_local_drink_24px
        } else if (tag.contains("meal_takeaway")) {
            R.drawable.ic_twotone_local_pizza_24px
        } else if (tag.contains("pharmacy")) {
            R.drawable.ic_twotone_local_pharmacy_24px
        } else if (tag.contains("meal_delivery") || tag.contains("moving_company")) {
            R.drawable.ic_twotone_local_shipping_24px
        } else if (tag.contains("parking")) {
            R.drawable.ic_twotone_local_parking_24px
        } else if (tag.contains("electronics_store")) {
            R.drawable.ic_twotone_local_printshop_24px
        } else if (tag.contains("laundry")) {
            R.drawable.ic_twotone_local_laundry_service_24px
        } else if (tag.contains("book_store") || tag.contains("library")) {
            R.drawable.ic_twotone_local_library_24px
        } else if (tag.contains("post_office")) {
            R.drawable.ic_twotone_local_post_office_24px
        } else if (tag.contains("movie_rental") || tag.contains("movie_theater")) {
            R.drawable.ic_twotone_local_movies_24px
        } else if (tag.contains("real_estate_agency") || tag.contains("establishment")) {
            R.drawable.ic_twotone_local_hotel_24px
        } else if (tag.contains("clothing_store") || tag.contains("home_goods_store")
                || tag.contains("shopping_mall") || tag.contains("shoe_store")) {
            R.drawable.ic_twotone_local_mall_24px
        } else if (tag.contains("food")) {
            R.drawable.ic_twotone_restaurant_24px
        } else {
            R.drawable.ic_twotone_place_24px
        }
    }

    fun setEventListener(eventListener: SimpleListener) {
        mEventListener = eventListener
    }
}
