package com.elementary.tasks.core.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import android.util.DisplayMetrics

import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions

import androidx.fragment.app.Fragment

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
abstract class BaseMapFragment : Fragment() {

    private var mContext: Activity? = null
    var themeUtil: ThemeUtil? = null
        private set
    var prefs: Prefs? = null
        private set

    private var mMapType = GoogleMap.MAP_TYPE_TERRAIN

    override fun getContext(): Activity? {
        return mContext
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mContext == null) {
            mContext = context as Activity?
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (mContext == null) {
            mContext = activity
        }
    }

    @JvmOverloads
    protected fun setStyle(map: GoogleMap, mapType: Int = mMapType) {
        mMapType = mapType
        map.setMapStyle(null)
        if (mapType == 3) {
            val res = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    activity!!, themeUtil!!.mapStyleJson))
            if (!res) {
                map.mapType = mapType
            }
        } else {
            map.mapType = mapType
        }
    }

    protected fun setMapType(map: GoogleMap, type: Int, function: Function?) {
        setStyle(map, type)
        prefs!!.mapType = type
        function?.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeUtil = ThemeUtil.getInstance(mContext)
        prefs = Prefs.getInstance(mContext)
        mMapType = prefs!!.mapType
    }

    protected fun getDescriptor(resId: Int): BitmapDescriptor {
        return if (Module.isLollipop) {
            getBitmapDescriptor(resId)
        } else {
            getBDPreLollipop(resId)
        }
    }

    private fun convertDpToPixel(dp: Float): Float {
        val metrics = mContext!!.resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }

    private fun getBDPreLollipop(@DrawableRes id: Int): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(WidgetUtils.getIcon(context, id))
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun getBitmapDescriptor(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = mContext!!.getDrawable(id)
        val h = convertDpToPixel(24f).toInt()
        val w = convertDpToPixel(24f).toInt()
        vectorDrawable!!.setBounds(0, 0, w, h)
        val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bm)
    }

    internal interface Function {
        fun apply()
    }
}
