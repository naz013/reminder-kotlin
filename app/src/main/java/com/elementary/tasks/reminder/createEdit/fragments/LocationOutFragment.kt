package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.ActionView
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_reminder_location_out.*

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

    private var mAdvancedMapFragment: AdvancedMapFragment? = null

    private var mTracker: LocationTracker? = null
    private var lastPos: LatLng? = null

    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            if (!hasAction) {
                reminderInterface?.setEventHint(getString(R.string.remind_me))
                reminderInterface?.setHasAutoExtra(false, "")
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            if (isMessageType) {
                reminderInterface?.setEventHint(getString(R.string.message))
                reminderInterface?.setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically))
            } else {
                reminderInterface?.setEventHint(getString(R.string.remind_me))
                reminderInterface?.setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically))
            }
        }
    }

    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {
            lastPos = place
            val location = SuperUtil.getAddress(place.latitude, place.longitude)
            currentLocation.text = location
        }

        override fun onZoomClick(isFull: Boolean) {
            reminderInterface?.setFullScreenMode(isFull)
        }

        override fun onBackClick() {
            val advancedMapFragment = mAdvancedMapFragment ?: return
            if (advancedMapFragment.isFullscreen) {
                advancedMapFragment.isFullscreen = false
                reminderInterface?.setFullScreenMode(false)
            }
            ViewUtils.fadeOutAnimation(mapContainer)
            ViewUtils.fadeInAnimation(specsContainer)
        }
    }

    private fun showPlaceOnMap() {
        val iFace = reminderInterface ?: return
        val item = iFace.reminder
        if (item != null) {
            if (!Reminder.isGpsType(item.type)) return
            val text = item.summary
            val jPlace = item.places[0]
            val latitude = jPlace.latitude
            val longitude = jPlace.longitude
            radius = jPlace.radius
            val advancedMapFragment = mAdvancedMapFragment ?: return
            advancedMapFragment.setMarkerRadius(radius)
            lastPos = LatLng(latitude, longitude)
            advancedMapFragment.addMarker(lastPos, text, true, true, radius)
            mapLocation.text = SuperUtil.getAddress(lastPos!!.latitude, lastPos!!.longitude)
            mapCheck.isChecked = true
        }
    }

    override fun recreateMarker() {
        mAdvancedMapFragment?.recreateMarker(radius)
    }

    override fun prepare(): Reminder? {
        if (super.prepare() == null) return null
        val iFace = reminderInterface ?: return null
        val map = mAdvancedMapFragment ?: return null
        var type = Reminder.BY_OUT
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(iFace.summary) && !isAction) {
            iFace.showSnackbar(getString(R.string.task_summary_is_empty))
            return null
        }
        val pos = lastPos
        if (pos == null) {
            iFace.showSnackbar(getString(R.string.you_dont_select_place))
            return null
        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_OUT_CALL
            } else {
                Reminder.BY_OUT_SMS
            }
        }
        var reminder = iFace.reminder
        if (reminder == null) {
            reminder = Reminder()
        }
        val place = Place(radius, map.markerStyle, pos.latitude, pos.longitude, iFace.summary, number, listOf())
        val places = listOf(place)
        reminder.places = places
        reminder.target = number
        reminder.type = type
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.setClear(iFace)
        if (attackDelay.isChecked) {
            val startTime = dateView.dateTime
            reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
            LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        } else {
            reminder.eventTime = ""
            reminder.startTime = ""
        }
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_location_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_custom_radius -> showRadiusPickerDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_location_out, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val advancedMapFragment = AdvancedMapFragment.newInstance(true, true, true, true,
                prefs.markerStyle, themeUtil.isDark)
        advancedMapFragment.setListener(mListener)
        advancedMapFragment.setCallback(object : MapCallback {
            override fun onMapReady() {
                showPlaceOnMap()
            }
        })
        fragmentManager!!.beginTransaction()
                .replace(mapFrame.id, advancedMapFragment)
                .addToBackStack(null)
                .commit()
        actionView.setListener(mActionListener)
        actionView.setActivity(activity!!)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })

        delayLayout.visibility = View.GONE
        mapContainer.visibility = View.GONE
        attackDelay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                delayLayout.visibility = View.VISIBLE
            else
                delayLayout.visibility = View.GONE
        }
        mapButton.setOnClickListener {
            if (mapCheck.isChecked) {
                toggleMap()
            }
            mapCheck.isChecked = true
        }
        currentCheck.setOnCheckedChangeListener { _, _ ->
            if (currentCheck.isChecked) {
                mapCheck.isChecked = false
                if (Permissions.checkPermission(activity!!, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                    mTracker = LocationTracker(context) { lat, lng ->
                        lastPos = LatLng(lat, lng)
                        val mLocation = SuperUtil.getAddress(lat, lng)
                        var text = reminderInterface!!.summary
                        if (TextUtils.isEmpty(text)) text = mLocation
                        currentLocation.text = mLocation
                        mAdvancedMapFragment?.addMarker(lastPos, text, true, true, radius)
                    }
                } else {
                    Permissions.requestPermission(activity!!, LOCATION, Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION)
                }
            }
        }
        mapCheck.setOnCheckedChangeListener { _, _ ->
            if (mapCheck.isChecked) {
                currentCheck.isChecked = false
                toggleMap()
                mTracker?.removeUpdates()
            }
        }
        currentCheck.isChecked = true
        editReminder()
    }

    private fun toggleMap() {
        if (mapContainer != null && mapContainer.visibility == View.VISIBLE) {
            ViewUtils.fadeOutAnimation(mapContainer)
            ViewUtils.fadeInAnimation(specsContainer)
        } else {
            ViewUtils.fadeOutAnimation(specsContainer)
            ViewUtils.fadeInAnimation(mapContainer)
            mAdvancedMapFragment?.showShowcase()
        }
    }

    override fun onBackPressed(): Boolean {
        return mAdvancedMapFragment == null || mAdvancedMapFragment!!.onBackPressed()
    }

    private fun editReminder() {
        val iFace = reminderInterface ?: return
        val reminder = iFace.reminder ?: return
        if (reminder.eventTime != "") {
            dateView.setDateTime(reminder.eventTime)
            attackDelay.isChecked = true
        }
        if (reminder.target != "") {
            actionView.setAction(true)
            actionView.number = reminder.target
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }

    private fun selectContact() {
        if (Permissions.checkPermission(activity!!, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(activity!!, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data!!.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
            actionView.number = number
        }
    }

    override fun onDetach() {
        super.onDetach()
        mTracker?.removeUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
            LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentCheck.isChecked = true
            }
            CONTACTS_ACTION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionView.setAction(true)
            }
        }
    }

    companion object {

        private const val TAG = "DateFragment"
        private const val CONTACTS = 132
        private const val LOCATION = 202
        const val CONTACTS_ACTION = 133
    }
}
