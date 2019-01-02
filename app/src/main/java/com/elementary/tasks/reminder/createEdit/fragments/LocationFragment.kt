package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.ActionView
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_reminder_location.*
import timber.log.Timber
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
class LocationFragment : RadiusTypeFragment() {

    private var mAdvancedMapFragment: AdvancedMapFragment? = null

    private var lastPos: LatLng? = null

    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {
            lastPos = place
        }

        override fun onZoomClick(isFull: Boolean) {
            reminderInterface.setFullScreenMode(isFull)
        }

        override fun onBackClick() {
            val map = mAdvancedMapFragment ?: return
            if (map.isFullscreen) {
                map.isFullscreen = false
                reminderInterface.setFullScreenMode(false)
            }
            ViewUtils.fadeOutAnimation(mapContainer)
            ViewUtils.fadeInAnimation(scrollView)
        }
    }

    private fun showPlaceOnMap() {
        val reminder = reminderInterface.reminder
        if (!Reminder.isGpsType(reminder.type)) return
        val text = reminder.summary
        if (reminder.places.isNotEmpty()) {
            val jPlace = reminder.places[0]
            val latitude = jPlace.latitude
            val longitude = jPlace.longitude
            radiusView.setRadiusValue(jPlace.radius)
            if (mAdvancedMapFragment != null) {
                mAdvancedMapFragment?.setMarkerRadius(radiusView.radius)
                lastPos = LatLng(latitude, longitude)
                mAdvancedMapFragment?.addMarker(lastPos, text, true, true, radiusView.radius)
                toggleMap()
            }
        }
    }

    override fun recreateMarker() {
        mAdvancedMapFragment?.recreateMarker(radiusView.radius)
    }

    override fun prepare(): Reminder? {
        val reminder = super.prepare() ?: return null
        val map = mAdvancedMapFragment ?: return null
        var type = if (enterCheck.isChecked) Reminder.BY_LOCATION else Reminder.BY_OUT
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(reminder.summary) && !isAction) {
            taskLayout.error = getString(R.string.task_summary_is_empty)
            taskLayout.isErrorEnabled = true
            return null
        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                reminderInterface.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                if (enterCheck.isChecked) Reminder.BY_LOCATION_CALL else Reminder.BY_OUT_CALL
            } else {
                if (enterCheck.isChecked) Reminder.BY_LOCATION_SMS else Reminder.BY_OUT_SMS
            }
        }
        val pos = lastPos
        if (pos == null) {
            reminderInterface.showSnackbar(getString(R.string.you_dont_select_place))
            return null
        }
        val places = ArrayList<Place>()
        places.add(Place(radiusView.radius, map.markerStyle, pos.latitude, pos.longitude, reminder.summary, number, listOf()))
        reminder.places = places
        reminder.target = number
        reminder.type = type
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.hasReminder = attackDelay.isChecked
        if (attackDelay.isChecked) {
            val startTime = dateView.dateTime
            reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
            Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true, true))
        } else {
            reminder.eventTime = ""
            reminder.startTime = ""
        }
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            reminderInterface.updateScroll(it)
        }
        moreLayout.isNestedScrollingEnabled = false

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

        this.mAdvancedMapFragment = advancedMapFragment

        if (Module.isPro) {
            ledView.visibility = View.VISIBLE
        } else {
            ledView.visibility = View.GONE
        }

        tuneExtraView.dialogues = dialogues
        tuneExtraView.hasAutoExtra = false

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

        melodyView.onFileSelectListener = {
            reminderInterface.selectMelody()
        }
        attachmentView.onFileSelectListener = {
            reminderInterface.attachFile()
        }
        groupView.onGroupSelectListener = {
            reminderInterface.selectGroup()
        }

        radiusView.setRadiusValue(prefs.radius)

        clearButton.setOnClickListener { addressField.setText("") }
        mapButton.setOnClickListener { toggleMap() }
        addressField.setOnItemClickListener { _, _, position, _ ->
            val sel = addressField.getAddress(position)
            val lat = sel.latitude
            val lon = sel.longitude
            val pos = LatLng(lat, lon)
            var title: String? = taskSummary.text.toString().trim()
            if (title != null && title.matches("".toRegex())) title = pos.toString()
            mAdvancedMapFragment?.addMarker(pos, title, true, true, radiusView.radius)
        }

        initPropertyFields()
        editReminder()
    }

    private fun initPropertyFields() {
        taskSummary.bindProperty(reminderInterface.reminder.summary) {
            reminderInterface.reminder.summary = it.trim()
        }
        dateView.bindProperty(reminderInterface.reminder.eventTime) {
            reminderInterface.reminder.eventTime = it
        }
        priorityView.bindProperty(reminderInterface.reminder.priority) {
            reminderInterface.reminder.priority = it
            updateHeader()
        }
        actionView.bindProperty(reminderInterface.reminder.target) {
            reminderInterface.reminder.target = it
            updateActions()
        }
        melodyView.bindProperty(reminderInterface.reminder.melodyPath) {
            reminderInterface.reminder.melodyPath = it
        }
        attachmentView.bindProperty(reminderInterface.reminder.attachmentFile) {
            reminderInterface.reminder.attachmentFile = it
        }
        loudnessView.bindProperty(reminderInterface.reminder.volume) {
            reminderInterface.reminder.volume = it
        }
        windowTypeView.bindProperty(reminderInterface.reminder.windowType) {
            reminderInterface.reminder.windowType = it
        }
        tuneExtraView.bindProperty(reminderInterface.reminder) {
            reminderInterface.reminder.copyExtra(it)
        }
        if (Module.isPro) {
            ledView.bindProperty(reminderInterface.reminder.color) {
                reminderInterface.reminder.color = it
            }
        }
    }

    private fun updateActions() {
        if (actionView.hasAction()) {
            tuneExtraView.hasAutoExtra = true
            if (actionView.type == ActionView.TYPE_MESSAGE) {
                tuneExtraView.hint = getString(R.string.enable_sending_sms_automatically)
            } else {
                tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
            }
        } else {
            tuneExtraView.hasAutoExtra = false
        }
    }

    private fun updateHeader() {
        cardSummary?.text = getSummary()
    }

    private fun toggleMap() {
        if (mapContainer != null && mapContainer.visibility == View.VISIBLE) {
            ViewUtils.fadeOutAnimation(mapContainer)
            ViewUtils.fadeInAnimation(scrollView)
        } else {
            ViewUtils.fadeOutAnimation(scrollView)
            ViewUtils.fadeInAnimation(mapContainer)
        }
    }

    override fun onBackPressed(): Boolean {
        return mAdvancedMapFragment == null || mAdvancedMapFragment!!.onBackPressed()
    }

    private fun editReminder() {
        val reminder = reminderInterface.reminder
        Timber.d("editReminder: %s", reminder.toString())
        groupView.reminderGroup = ReminderGroup().apply {
            this.groupColor = reminder.groupColor
            this.groupTitle = reminder.groupTitle
            this.groupUuId = reminder.groupUuId
        }
        if (reminder.eventTime != "" && reminder.hasReminder) {
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
        if (Reminder.isBase(reminder.type, Reminder.BY_OUT)) {
            leaveCheck.isChecked = true
        } else {
            enterCheck.isChecked = true
        }
        updateHeader()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
            CONTACTS_ACTION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionView.setAction(true)
            }
        }
    }

    override fun onGroupUpdate(reminderGroup: ReminderGroup) {
        super.onGroupUpdate(reminderGroup)
        groupView?.reminderGroup = reminderGroup
        updateHeader()
    }

    override fun onMelodySelect(path: String) {
        super.onMelodySelect(path)
        melodyView.file = path
    }

    override fun onAttachmentSelect(path: String) {
        super.onAttachmentSelect(path)
        attachmentView.file = path
    }

    companion object {
        private const val CONTACTS = 122
        const val CONTACTS_ACTION = 123
    }
}
