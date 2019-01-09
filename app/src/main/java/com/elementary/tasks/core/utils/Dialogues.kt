package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.view_color_slider.view.*
import javax.inject.Inject
import javax.inject.Singleton


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
@Singleton
class Dialogues @Inject constructor(private val themeUtil: ThemeUtil) {

    fun showColorDialog(activity: Activity, current: Int, title: String,
                        colors: IntArray = themeUtil.colorsForSlider(),
                        onDone: (Int) -> Unit) {
        val builder = getDialog(activity)
        builder.setTitle(title)
        val bind = LayoutInflater.from(activity).inflate(R.layout.view_color_slider, null, false)
        bind.colorSlider.setColors(colors)
        bind.colorSlider.setSelection(current)
        builder.setView(bind)
        builder.setPositiveButton(R.string.save) { dialog, _ ->
            val selected = bind.colorSlider.selectedItem
            dialog.dismiss()
            onDone.invoke(selected)
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity)
    }

    fun showRadiusDialog(activity: Activity, current: Int, listener: OnValueSelectedListener<Int>) {
        val builder = getDialog(activity)
        builder.setTitle(R.string.radius)
        val b = LayoutInflater.from(activity).inflate(R.layout.dialog_with_seek_and_title, null, false)
        b.seekBar.max = MAX_DEF_RADIUS
        while (b.seekBar.max < current && b.seekBar.max < MAX_RADIUS) {
            b.seekBar.max = b.seekBar.max + 1000
        }
        if (current > MAX_RADIUS) {
            b.seekBar.max = MAX_RADIUS
        }
        b.seekBar.max = current * 2
        if (current == 0) {
            b.seekBar.max = MAX_DEF_RADIUS
        }
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = listener.getTitle(progress)
                val perc = progress.toFloat() / b.seekBar.max.toFloat() * 100f
                if (perc > 95f && b.seekBar.max < MAX_RADIUS) {
                    b.seekBar.max = b.seekBar.max + 1000
                } else if (perc < 15f && b.seekBar.max > 5000) {
                    b.seekBar.max = b.seekBar.max - 1000
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = current
        b.titleView.text = listener.getTitle(current)
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ -> listener.onSelected(b.seekBar.progress) }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity)
    }

    fun getDialog(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context, themeUtil.dialogStyle)
    }

    fun showLCAM(context: Context, listener: ((Int) -> Unit)?, vararg actions: String) {
        val builder = getDialog(context)
        builder.setItems(actions) { dialog, item ->
            dialog.dismiss()
            listener?.invoke(item)
        }
        val alert = builder.create()
        alert.show()
    }

    interface OnValueSelectedListener<T> {
        fun onSelected(t: T)
        fun getTitle(t: T): String
    }

    companion object {
        private const val MAX_RADIUS = 100000
        private const val MAX_DEF_RADIUS = 5000

        fun showPopup(anchor: View,
                      listener: ((Int) -> Unit)?, vararg actions: String) {
            val popupMenu = PopupMenu(anchor.context, anchor)
            popupMenu.setOnMenuItemClickListener { item ->
                listener?.invoke(item.order)
                true
            }
            for (i in actions.indices) {
                popupMenu.menu.add(1, i + 1000, i, actions[i])
            }
            popupMenu.show()
        }

        fun setFullWidthDialog(dialog: AlertDialog, activity: Activity) {
            val window = dialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setLayout((getScreenWidth(activity) * .9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        private fun getScreenWidth(activity: Activity): Int {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            return size.x
        }
    }
}
