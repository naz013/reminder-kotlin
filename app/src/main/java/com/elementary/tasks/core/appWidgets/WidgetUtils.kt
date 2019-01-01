package com.elementary.tasks.core.appWidgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import android.widget.RemoteViews

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module

import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.content.ContextCompat
import com.elementary.tasks.core.utils.ViewUtils

/**
 * Copyright 2015 Nazar Suhovich
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
object WidgetUtils {

    fun initButton(context: Context, rv: RemoteViews, @DrawableRes iconId: Int, @IdRes viewId: Int,
                   cls: Class<*>, extras: ((Intent) -> Intent)? = null) {
        var configIntent = Intent(context, cls)
        if (extras != null) {
            configIntent = extras.invoke(configIntent)
        }
        extras?.invoke(configIntent)
        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
        rv.setOnClickPendingIntent(viewId, configPendingIntent)
        WidgetUtils.setIcon(context, rv, iconId, viewId)
    }

    fun initButton(context: Context, rv: RemoteViews, @DrawableRes iconId: Int, @IdRes viewId: Int,
                   cls: Class<*>) {
        val configIntent = Intent(context, cls)
        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
        rv.setOnClickPendingIntent(viewId, configPendingIntent)
        WidgetUtils.setIcon(context, rv, iconId, viewId)
    }

    fun setIcon(context: Context, rv: RemoteViews, @DrawableRes iconId: Int, @IdRes viewId: Int, @ColorRes color: Int) {
        rv.setImageViewBitmap(viewId, ViewUtils.createIcon(context, iconId, ContextCompat.getColor(context, color)))
    }

    fun setIcon(context: Context, rv: RemoteViews, @DrawableRes iconId: Int, @IdRes viewId: Int) {
        if (Module.isLollipop) {
            rv.setImageViewResource(viewId, iconId)
        } else {
            rv.setImageViewBitmap(viewId, getIcon(context, iconId))
        }
    }

    @SuppressLint("RestrictedApi")
    fun getIcon(context: Context, @DrawableRes id: Int): Bitmap {
        val d = AppCompatDrawableManager.get().getDrawable(context, id)
        val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        return b
    }

    @ColorRes
    fun getColor(code: Int): Int {
        var color = 0
        when (code) {
            0 -> color = R.color.whitePrimary
            1 -> color = R.color.redPrimary
            2 -> color = R.color.purplePrimary
            3 -> color = R.color.greenLightPrimary
            4 -> color = R.color.greenPrimary
            5 -> color = R.color.blueLightPrimary
            6 -> color = R.color.bluePrimary
            7 -> color = R.color.yellowPrimary
            8 -> color = R.color.orangePrimary
            9 -> color = R.color.cyanPrimary
            10 -> color = R.color.pinkPrimary
            11 -> color = R.color.tealPrimary
            12 -> color = R.color.amberPrimary
            13 -> color = android.R.color.transparent
            else -> if (Module.isPro) {
                when (code) {
                    14 -> color = R.color.purpleDeepPrimary
                    15 -> color = R.color.orangeDeepPrimary
                    16 -> color = R.color.limePrimary
                    17 -> color = R.color.indigoPrimary
                }
            } else {
                color = R.color.bluePrimary
            }
        }
        return color
    }

    @DrawableRes
    fun getDrawable(code: Int): Int {
        var drawable = 0
        when (code) {
            0 -> drawable = R.drawable.rectangle_stroke_red
            1 -> drawable = R.drawable.rectangle_stroke_purple
            2 -> drawable = R.drawable.rectangle_stroke_light_green
            3 -> drawable = R.drawable.rectangle_stroke_green
            4 -> drawable = R.drawable.rectangle_stroke_light_blue
            5 -> drawable = R.drawable.rectangle_stroke_blue
            6 -> drawable = R.drawable.rectangle_stroke_yellow
            7 -> drawable = R.drawable.rectangle_stroke_orange
            8 -> drawable = R.drawable.rectangle_stroke_cyan
            9 -> drawable = R.drawable.rectangle_stroke
            10 -> drawable = R.drawable.rectangle_stroke_teal
            11 -> drawable = R.drawable.rectangle_stroke_amber
            12 -> drawable = R.drawable.rectangle_stroke_transparent
            else -> if (Module.isPro) {
                when (code) {
                    13 -> drawable = R.drawable.rectangle_stroke_deep_purple
                    14 -> drawable = R.drawable.rectangle_stroke_deep_orange
                    15 -> drawable = R.drawable.rectangle_stroke_lime
                    16 -> drawable = R.drawable.rectangle_stroke_indigo
                }
            } else {
                drawable = R.drawable.rectangle_stroke_blue
            }
        }
        return drawable
    }

    @DrawableRes
    fun newWidgetBg(code: Int): Int {
        return when (code) {
            0 -> R.drawable.widget_bg_transparent
            1 -> R.drawable.widget_bg_white
            2 -> R.drawable.widget_bg_light1
            3 -> R.drawable.widget_bg_light2
            4 -> R.drawable.widget_bg_light3
            5 -> R.drawable.widget_bg_light4
            6 -> R.drawable.widget_bg_dark1
            7 -> R.drawable.widget_bg_dark2
            8 -> R.drawable.widget_bg_dark3
            9 -> R.drawable.widget_bg_dark4
            10 -> R.drawable.widget_bg_black
            else -> R.drawable.widget_bg_black
        }
    }

    fun isDarkBg(code: Int): Boolean = code > 5
}
