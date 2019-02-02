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
import com.elementary.tasks.databinding.DialogBottomColorSliderBinding
import com.elementary.tasks.databinding.DialogBottomSeekAndTitleBinding
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    fun showColorBottomDialog(activity: Activity, current: Int, colors: IntArray = themeUtil.colorsForSlider(),
                        onChange: (Int) -> Unit) {
        val dialog = BottomSheetDialog(activity)
        val b = DialogBottomColorSliderBinding.inflate(LayoutInflater.from(activity))
        b.colorSlider.setColors(colors)
        b.colorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        b.colorSlider.setSelection(current)
        b.colorSlider.setListener { i, _ ->
            onChange.invoke(i)
        }
        dialog.setContentView(b.root)
        dialog.show()
    }

    fun showRadiusBottomDialog(activity: Activity, current: Int, listener: (Int) -> String) {
        val dialog = BottomSheetDialog(activity)
        val b = DialogBottomSeekAndTitleBinding.inflate(LayoutInflater.from(activity))
        b.seekBar.max = MAX_DEF_RADIUS
        if (b.seekBar.max < current && b.seekBar.max < MAX_RADIUS) {
            b.seekBar.max = (current + (b.seekBar.max * 0.2)).toInt()
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
                b.titleView.text = listener.invoke(progress)
                val perc = progress.toFloat() / b.seekBar.max.toFloat() * 100f
                if (perc > 95f && b.seekBar.max < MAX_RADIUS) {
                    b.seekBar.max = (b.seekBar.max + (b.seekBar.max * 0.2)).toInt()
                } else if (perc < 10f && b.seekBar.max > 5000) {
                    b.seekBar.max = (b.seekBar.max - (b.seekBar.max * 0.2)).toInt()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = current
        b.titleView.text = listener.invoke(current)
        dialog.setContentView(b.root)
        dialog.show()
    }

    fun showColorDialog(activity: Activity, current: Int, title: String,
                        colors: IntArray = themeUtil.colorsForSlider(),
                        onDone: (Int) -> Unit) {
        val builder = getDialog(activity)
        builder.setTitle(title)
        val bind = com.elementary.tasks.databinding.ViewColorSliderBinding.inflate(LayoutInflater.from(activity))
        bind.colorSlider.setColors(colors)
        bind.colorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        bind.colorSlider.setSelection(current)
        builder.setView(bind.root)
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
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(activity))
        b.seekBar.max = MAX_DEF_RADIUS
        if (b.seekBar.max < current && b.seekBar.max < MAX_RADIUS) {
            b.seekBar.max = (current + (b.seekBar.max * 0.2)).toInt()
        }
        if (current > MAX_RADIUS) {
            b.seekBar.max = MAX_RADIUS
        }
        if (current == 0) {
            b.seekBar.max = MAX_DEF_RADIUS
        }
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = listener.getTitle(progress)
                val perc = progress.toFloat() / b.seekBar.max.toFloat() * 100f
                if (perc > 95f && b.seekBar.max < MAX_RADIUS) {
                    b.seekBar.max = (b.seekBar.max + (b.seekBar.max * 0.2)).toInt()
                } else if (perc < 10f && b.seekBar.max > 5000) {
                    b.seekBar.max = (b.seekBar.max - (b.seekBar.max * 0.2)).toInt()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = current
        b.titleView.text = listener.getTitle(current)
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { _, _ -> listener.onSelected(b.seekBar.progress) }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity)
    }

    fun getDialog(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context, themeUtil.dialogStyle)
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
