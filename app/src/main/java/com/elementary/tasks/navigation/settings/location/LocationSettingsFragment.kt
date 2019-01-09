package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.places.list.PlacesFragment
import kotlinx.android.synthetic.main.dialog_tracking_settings_layout.view.*
import kotlinx.android.synthetic.main.fragment_settings_location.*
import java.util.*

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

    override fun layoutRes(): Int = R.layout.fragment_settings_location

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            setScroll(it)
        }
        initMapTypePrefs()
        initMarkerStylePrefs()
        trackerPrefs.setOnClickListener { showTrackerOptionsDialog() }
        notificationOptionPrefs.setOnClickListener { changeNotificationPrefs() }
        placesPrefs.setOnClickListener { openPlacesScreen() }
        notificationOptionPrefs.isChecked = prefs.isDistanceNotificationEnabled
        initRadiusPrefs()
    }

    private fun openPlacesScreen() {
        callback?.openFragment(PlacesFragment.newInstance(), getString(R.string.places))
    }

    private fun initMapStylePrefs() {
        mapStylePrefs.setOnClickListener { openMapStylesFragment() }
        mapStylePrefs.setDetailText(getString(themeUtil.styleName))
        mapStylePrefs.setViewResource(themeUtil.mapStylePreview)
        mapStylePrefs.isEnabled = prefs.mapType == 3
    }

    private fun openMapStylesFragment() {
        callback?.openFragment(MapStyleFragment.newInstance(), getString(R.string.map_style))
    }

    private fun initMarkerStylePrefs() {
        markerStylePrefs.setOnClickListener { showStyleDialog() }
        showMarkerStyle()
    }

    private fun showStyleDialog() {
        dialogues.showColorDialog(activity!!, prefs.markerStyle,
                getString(R.string.style_of_marker), themeUtil.colorsForSlider()) {
            prefs.markerStyle = it
            showMarkerStyle()
        }
    }

    private fun showMarkerStyle() {
        val pointer = DrawableHelper.withContext(context!!)
                .withDrawable(R.drawable.ic_twotone_place_24px)
                .withColor(themeUtil.getNoteLightColor(prefs.markerStyle))
                .tint()
                .get()
        markerStylePrefs.setViewDrawable(pointer)
    }

    private fun initMapTypePrefs() {
        mapTypePrefs.setOnClickListener { showMapTypeDialog() }
        showMapType()
    }

    private fun showMapType() {
        val types = resources.getStringArray(R.array.map_types)
        mapTypePrefs.setDetailText(types[getPosition(prefs.mapType)])
    }

    private fun initRadiusPrefs() {
        radiusPrefs.setOnClickListener { showRadiusPickerDialog() }
        showRadius()
    }

    override fun onBackStackResume() {
        super.onBackStackResume()
        showMarkerStyle()
        initMapStylePrefs()
    }

    override fun getTitle(): String = getString(R.string.location)

    private fun showTrackerOptionsDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.tracking_settings)
        val b = layoutInflater.inflate(R.layout.dialog_tracking_settings_layout, null)
        b.distanceBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.distanceTitle.text = String.format(Locale.getDefault(), getString(R.string.x_meters), (progress + 1).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        val distance = prefs.trackDistance - 1
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
        val time = prefs.trackTime - 1
        b.timeBar.progress = time
        b.timeTitle.text = String.format(Locale.getDefault(), getString(R.string.x_seconds), (time + 1).toString())
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.trackDistance = b.distanceBar.progress + 1
            prefs.trackTime = b.timeBar.progress + 1
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun showMapTypeDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.map_type))
        val types = arrayOf(getString(R.string.normal), getString(R.string.satellite), getString(R.string.terrain), getString(R.string.hybrid))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, types)
        val type = prefs.mapType
        mItemSelect = getPosition(type)
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
            prefs.mapType = mItemSelect + 1
            showMapType()
            initMapStylePrefs()
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun getPosition(type: Int): Int {
        return when (type) {
            Constants.MAP_SATELLITE -> 1
            Constants.MAP_TERRAIN -> 2
            Constants.MAP_HYBRID -> 3
            else -> 0
        }
    }

    private fun changeNotificationPrefs() {
        val isChecked = notificationOptionPrefs.isChecked
        notificationOptionPrefs.isChecked = !isChecked
        prefs.isDistanceNotificationEnabled = !isChecked
    }

    private fun showRadius() {
        radiusPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
                prefs.radius.toString()))
    }

    private fun showRadiusPickerDialog() {
        val radius = prefs.radius
        dialogues.showRadiusDialog(activity!!, radius, object : Dialogues.OnValueSelectedListener<Int> {
            override fun onSelected(t: Int) {
                prefs.radius = t
                showRadius()
            }

            override fun getTitle(t: Int): String {
                return String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
                        t.toString())
            }
        })
    }
}
