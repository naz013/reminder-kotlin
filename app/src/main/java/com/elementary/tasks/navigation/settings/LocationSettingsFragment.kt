package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.DialogTrackingSettingsLayoutBinding
import com.elementary.tasks.databinding.FragmentSettingsLocationBinding
import com.elementary.tasks.navigation.settings.location.MapStyleFragment
import com.elementary.tasks.navigation.settings.location.MarkerStyleFragment

import java.util.Locale

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

class LocationSettingsFragment : BaseSettingsFragment() {

    private var mItemSelect: Int = 0

    private var binding: FragmentSettingsLocationBinding? = null
    private val mRadiusClick = { view -> showRadiusPickerDialog() }
    private val mNotificationClick = { view -> changeNotificationPrefs() }
    private val mMapTypeClick = { view -> showMapTypeDialog() }
    private val mStyleClick = { view -> replaceFragment(MarkerStyleFragment(), getString(R.string.style_of_marker)) }
    private val mTrackClick = { view -> showTrackerOptionsDialog() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsLocationBinding.inflate(inflater, container, false)
        initMapTypePrefs()
        initMarkerStylePrefs()
        binding!!.trackerPrefs.setOnClickListener(mTrackClick)
        binding!!.notificationOptionPrefs.setOnClickListener(mNotificationClick)
        binding!!.notificationOptionPrefs.isChecked = prefs!!.isDistanceNotificationEnabled
        initRadiusPrefs()
        return binding!!.root
    }

    private fun initMapStylePrefs() {
        binding!!.mapStylePrefs.setOnClickListener { v -> openMapStylesFragment() }
        binding!!.mapStylePrefs.setDetailText(getString(ThemeUtil.getInstance(context).styleName))
        binding!!.mapStylePrefs.setViewResource(ThemeUtil.getInstance(context).mapStylePreview)
        binding!!.mapStylePrefs.isEnabled = Prefs.getInstance(context).mapType == 3
    }

    private fun openMapStylesFragment() {
        if (callback != null) {
            callback!!.replaceFragment(MapStyleFragment.newInstance(), getString(R.string.map_style))
        }
    }

    private fun initMarkerStylePrefs() {
        binding!!.markerStylePrefs.setOnClickListener(mStyleClick)
        showMarkerStyle()
    }

    private fun showMarkerStyle() {
        binding!!.markerStylePrefs.setViewResource(ThemeUtil.getInstance(context).markerStyle)
    }

    private fun initMapTypePrefs() {
        binding!!.mapTypePrefs.setOnClickListener(mMapTypeClick)
        showMapType()
    }

    private fun showMapType() {
        val types = resources.getStringArray(R.array.map_types)
        binding!!.mapTypePrefs.setDetailText(types[getPosition(prefs!!.mapType)])
    }

    private fun initRadiusPrefs() {
        binding!!.radiusPrefs.setOnClickListener(mRadiusClick)
        showRadius()
    }

    override fun onResume() {
        super.onResume()
        showMarkerStyle()
        initMapStylePrefs()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.location))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun showTrackerOptionsDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.tracking_settings)
        val b = DialogTrackingSettingsLayoutBinding.inflate(LayoutInflater.from(context))
        b.distanceBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.distanceTitle.text = String.format(Locale.getDefault(), getString(R.string.x_meters), (progress + 1).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        val distance = prefs!!.trackDistance - 1
        b.distanceBar.progress = distance
        b.distanceTitle.text = String.format(Locale.getDefault(), getString(R.string.x_meters), (distance + 1).toString())
        b.timeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.timeTitle.text = String.format(Locale.getDefault(), getString(R.string.x_seconds), (progress + 1).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        val time = prefs!!.trackTime - 1
        b.timeBar.progress = time
        b.timeTitle.text = String.format(Locale.getDefault(), getString(R.string.x_seconds), (time + 1).toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.trackDistance = b.distanceBar.progress + 1
            prefs!!.trackTime = b.timeBar.progress + 1
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showMapTypeDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.map_type))
        val types = arrayOf(getString(R.string.normal), getString(R.string.satellite), getString(R.string.terrain), getString(R.string.hybrid))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        val type = prefs!!.mapType
        mItemSelect = getPosition(type)
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
            prefs!!.mapType = mItemSelect + 1
            showMapType()
            initMapStylePrefs()
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun getPosition(type: Int): Int {
        val mItemSelect: Int
        if (type == Constants.MAP_SATELLITE) {
            mItemSelect = 1
        } else if (type == Constants.MAP_TERRAIN) {
            mItemSelect = 2
        } else if (type == Constants.MAP_HYBRID) {
            mItemSelect = 3
        } else {
            mItemSelect = 0
        }
        return mItemSelect
    }

    private fun changeNotificationPrefs() {
        val isChecked = binding!!.notificationOptionPrefs.isChecked
        binding!!.notificationOptionPrefs.isChecked = !isChecked
        prefs!!.isDistanceNotificationEnabled = !isChecked
    }

    private fun showRadius() {
        binding!!.radiusPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
                prefs!!.radius.toString()))
    }

    private fun showRadiusPickerDialog() {
        val radius = prefs!!.radius
        Dialogues.showRadiusDialog(context!!, radius, object : Dialogues.OnValueSelectedListener<Int> {
            override fun onSelected(integer: Int?) {
                prefs!!.radius = integer
                showRadius()
            }

            override fun getTitle(integer: Int?): String {
                return String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
                        integer.toString())
            }
        })
    }
}
