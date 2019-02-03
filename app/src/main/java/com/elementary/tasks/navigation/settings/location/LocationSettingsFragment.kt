package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.DialogTrackingSettingsLayoutBinding
import com.elementary.tasks.databinding.FragmentSettingsLocationBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.places.list.PlacesFragment
import com.google.android.gms.maps.GoogleMap
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
class LocationSettingsFragment : BaseSettingsFragment<FragmentSettingsLocationBinding>() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_location

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }
        initMapTypePrefs()
        initMarkerStylePrefs()
        binding.trackerPrefs.setOnClickListener { showTrackerOptionsDialog() }
        binding.notificationOptionPrefs.setOnClickListener { changeNotificationPrefs() }
        if (Module.hasLocation(context!!)) {
            binding.placesPrefs.setOnClickListener { openPlacesScreen() }
            binding.placesPrefs.visibility = View.VISIBLE
        } else {
            binding.placesPrefs.visibility = View.GONE
        }
        binding.notificationOptionPrefs.isChecked = prefs.isDistanceNotificationEnabled
        initRadiusPrefs()
    }

    private fun openPlacesScreen() {
        callback?.openFragment(PlacesFragment.newInstance(), getString(R.string.places))
    }

    private fun initMapStylePrefs() {
        binding.mapStylePrefs.setOnClickListener { openMapStylesFragment() }
        binding.mapStylePrefs.setDetailText(getString(themeUtil.styleName))
        binding.mapStylePrefs.setViewResource(themeUtil.mapStylePreview)
        binding.mapStylePrefs.isEnabled = prefs.mapType == GoogleMap.MAP_TYPE_NORMAL
    }

    private fun openMapStylesFragment() {
        callback?.openFragment(MapStyleFragment.newInstance(), getString(R.string.map_style))
    }

    private fun initMarkerStylePrefs() {
        binding.markerStylePrefs.setOnClickListener { showStyleDialog() }
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
        binding.markerStylePrefs.setViewDrawable(pointer)
    }

    private fun initMapTypePrefs() {
        binding.mapTypePrefs.setOnClickListener { showMapTypeDialog() }
        showMapType()
    }

    private fun showMapType() {
        val types = resources.getStringArray(R.array.map_types)
        binding.mapTypePrefs.setDetailText(types[getPosition(prefs.mapType)])
    }

    private fun initRadiusPrefs() {
        binding.radiusPrefs.setOnClickListener { showRadiusPickerDialog() }
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
        val b = DialogTrackingSettingsLayoutBinding.inflate(layoutInflater)
        val time = prefs.trackTime - 1
        b.timeBar.progress = time
        b.timeTitle.text = String.format(Locale.getDefault(), getString(R.string.x_seconds), (time + 1).toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { _, _ ->
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
            GoogleMap.MAP_TYPE_SATELLITE -> 1
            GoogleMap.MAP_TYPE_TERRAIN -> 2
            GoogleMap.MAP_TYPE_HYBRID -> 3
            else -> 0
        }
    }

    private fun changeNotificationPrefs() {
        val isChecked = binding.notificationOptionPrefs.isChecked
        binding.notificationOptionPrefs.isChecked = !isChecked
        prefs.isDistanceNotificationEnabled = !isChecked
    }

    private fun showRadius() {
        binding.radiusPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
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
