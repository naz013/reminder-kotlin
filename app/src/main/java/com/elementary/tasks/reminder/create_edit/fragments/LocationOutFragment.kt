package com.elementary.tasks.reminder.create_edit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderLocationOutBinding
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.google.android.gms.maps.model.LatLng

import java.util.Collections

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

class LocationOutFragment : RadiusTypeFragment() {

    private var binding: FragmentReminderLocationOutBinding? = null
    private var advancedMapFragment: AdvancedMapFragment? = null

    private var mTracker: LocationTracker? = null
    private var lastPos: LatLng? = null

    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            if (!hasAction) {
                `interface`!!.setEventHint(getString(R.string.remind_me))
                `interface`!!.setHasAutoExtra(false, null)
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            if (isMessageType) {
                `interface`!!.setEventHint(getString(R.string.message))
                `interface`!!.setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically))
            } else {
                `interface`!!.setEventHint(getString(R.string.remind_me))
                `interface`!!.setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically))
            }
        }
    }
    private val mCallback = MapCallback { this.showPlaceOnMap() }

    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {
            lastPos = place
            val location = SuperUtil.getAddress(place.latitude, place.longitude)
            binding!!.currentLocation.text = location
        }

        override fun onZoomClick(isFull: Boolean) {
            `interface`!!.setFullScreenMode(isFull)
        }

        override fun onBackClick() {
            if (advancedMapFragment!!.isFullscreen) {
                advancedMapFragment!!.isFullscreen = false
                `interface`!!.setFullScreenMode(false)
            }
            ViewUtils.fadeOutAnimation(binding!!.mapContainer)
            ViewUtils.fadeInAnimation(binding!!.specsContainer)
        }
    }
    private val mLocationCallback = LocationTracker.Callback { lat, lon ->
        lastPos = LatLng(lat, lon)
        val _Location = SuperUtil.getAddress(lat, lon)
        var text = `interface`!!.summary
        if (TextUtils.isEmpty(text)) text = _Location
        binding!!.currentLocation.text = _Location
        if (advancedMapFragment != null) {
            advancedMapFragment!!.addMarker(lastPos, text, true, true, radius)
        }
    }

    private fun showPlaceOnMap() {
        if (`interface`!!.reminder != null) {
            val item = `interface`!!.reminder
            if (!Reminder.isGpsType(item.type)) return
            val text = item.summary
            val jPlace = item.places[0]
            val latitude = jPlace.latitude
            val longitude = jPlace.longitude
            radius = jPlace.radius
            if (advancedMapFragment != null) {
                advancedMapFragment!!.setMarkerRadius(radius)
                lastPos = LatLng(latitude, longitude)
                advancedMapFragment!!.addMarker(lastPos, text, true, true, radius)
            }
            binding!!.mapLocation.text = SuperUtil.getAddress(lastPos!!.latitude, lastPos!!.longitude)
            binding!!.mapCheck.isChecked = true
        }
    }

    override fun recreateMarker() {
        advancedMapFragment!!.recreateMarker(radius)
    }

    override fun prepare(): Reminder? {
        if (super.prepare() == null) return null
        if (`interface` == null) return null
        var reminder: Reminder? = `interface`!!.reminder
        var type = Reminder.BY_OUT
        val isAction = binding!!.actionView.hasAction()
        if (TextUtils.isEmpty(`interface`!!.summary) && !isAction) {
            `interface`!!.showSnackbar(getString(R.string.task_summary_is_empty))
            return null
        }
        if (lastPos == null) {
            `interface`!!.showSnackbar(getString(R.string.you_dont_select_place))
            return null
        }
        var number: String? = null
        if (isAction) {
            number = binding!!.actionView.number
            if (TextUtils.isEmpty(number)) {
                `interface`!!.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            if (binding!!.actionView.type == ActionView.TYPE_CALL) {
                type = Reminder.BY_OUT_CALL
            } else {
                type = Reminder.BY_OUT_SMS
            }
        }
        if (reminder == null) {
            reminder = Reminder()
        }
        val place = Place(radius, advancedMapFragment!!.markerStyle, lastPos!!.latitude, lastPos!!.longitude, `interface`!!.summary, number, null)
        val places = listOf(place)
        reminder.places = places
        reminder.target = number
        reminder.type = type
        reminder.isExportToCalendar = false
        reminder.isExportToTasks = false
        reminder.setClear(`interface`)
        if (binding!!.attackDelay.isChecked) {
            val startTime = binding!!.dateView.dateTime
            reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
            LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        } else {
            reminder.eventTime = null
            reminder.startTime = null
        }
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_location_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_custom_radius -> showRadiusPickerDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReminderLocationOutBinding.inflate(inflater, container, false)
        advancedMapFragment = AdvancedMapFragment.newInstance(true, true, true, true,
                Prefs.getInstance(context).markerStyle, ThemeUtil.getInstance(context).isDark)
        advancedMapFragment!!.setListener(mListener)
        advancedMapFragment!!.setCallback(mCallback)
        fragmentManager!!.beginTransaction()
                .replace(binding!!.mapFrame.id, advancedMapFragment!!)
                .addToBackStack(null)
                .commit()
        binding!!.actionView.setListener(mActionListener)
        binding!!.actionView.setActivity(activity)
        binding!!.actionView.setContactClickListener { view -> selectContact() }

        binding!!.delayLayout.visibility = View.GONE
        binding!!.mapContainer.visibility = View.GONE
        binding!!.attackDelay.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                binding!!.delayLayout.visibility = View.VISIBLE
            else
                binding!!.delayLayout.visibility = View.GONE
        }
        binding!!.mapButton.setOnClickListener { view ->
            if (binding!!.mapCheck.isChecked) {
                toggleMap()
            }
            binding!!.mapCheck.isChecked = true
        }
        binding!!.currentCheck.setOnCheckedChangeListener { compoundButton, b ->
            if (binding!!.currentCheck.isChecked) {
                binding!!.mapCheck.isChecked = false
                if (Permissions.checkPermission(activity, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                    mTracker = LocationTracker(context, mLocationCallback)
                } else {
                    Permissions.requestPermission(activity, LOCATION, Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION)
                }
            }
        }
        binding!!.mapCheck.setOnCheckedChangeListener { compoundButton, b ->
            if (binding!!.mapCheck.isChecked) {
                binding!!.currentCheck.isChecked = false
                toggleMap()
                if (mTracker != null) {
                    mTracker!!.removeUpdates()
                }
            }
        }
        binding!!.currentCheck.isChecked = true
        editReminder()
        return binding!!.root
    }

    private fun toggleMap() {
        if (binding!!.mapContainer != null && binding!!.mapContainer.visibility == View.VISIBLE) {
            ViewUtils.fadeOutAnimation(binding!!.mapContainer)
            ViewUtils.fadeInAnimation(binding!!.specsContainer)
        } else {
            ViewUtils.fadeOutAnimation(binding!!.specsContainer)
            ViewUtils.fadeInAnimation(binding!!.mapContainer)
            if (advancedMapFragment != null) {
                advancedMapFragment!!.showShowcase()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return advancedMapFragment == null || advancedMapFragment!!.onBackPressed()
    }

    private fun editReminder() {
        if (`interface`!!.reminder == null) return
        val reminder = `interface`!!.reminder
        if (reminder.eventTime != null) {
            binding!!.dateView.setDateTime(reminder.eventTime)
            binding!!.attackDelay.isChecked = true
        }
        if (reminder.target != null) {
            binding!!.actionView.setAction(true)
            binding!!.actionView.number = reminder.target
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                binding!!.actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                binding!!.actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }

    private fun selectContact() {
        if (Permissions.checkPermission(activity, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(activity, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data!!.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
            binding!!.actionView.number = number
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (mTracker != null) mTracker!!.removeUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        binding!!.actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 0) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
            LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding!!.currentCheck.isChecked = true
            }
            CONTACTS_ACTION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding!!.actionView.setAction(true)
            }
        }
    }

    companion object {

        private val TAG = "DateFragment"
        private val CONTACTS = 132
        private val LOCATION = 202
        val CONTACTS_ACTION = 133
    }
}
