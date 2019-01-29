package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.PlacesMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.views.ActionView
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_reminder_place.*
import timber.log.Timber

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
class PlacesFragment : RadiusTypeFragment() {

    private var mPlacesMap: PlacesMapFragment? = null
    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {
        }

        override fun onZoomClick(isFull: Boolean) {
            iFace.setFullScreenMode(isFull)
        }

        override fun onBackClick() {
            if (!isTablet()) {
                val map = mPlacesMap ?: return
                if (map.isFullscreen) {
                    map.isFullscreen = false
                    iFace.setFullScreenMode(false)
                }
                if (mapContainer.visibility == View.VISIBLE) {
                    ViewUtils.fadeOutAnimation(mapContainer)
                    ViewUtils.fadeInAnimation(scrollView)
                }
            }
        }
    }

    override fun recreateMarker() {
        mPlacesMap?.recreateMarker()
    }

    override fun prepare(): Reminder? {
        val reminder = super.prepare() ?: return null
        val map = mPlacesMap ?: return null
        var type = Reminder.BY_PLACES
        val places = map.places
        if (places.isEmpty()) {
            iFace.showSnackbar(getString(R.string.you_dont_select_place))
            return null
        }
        if (TextUtils.isEmpty(reminder.summary)) {
            taskLayout.error = getString(R.string.task_summary_is_empty)
            taskLayout.isErrorEnabled = true
            map.invokeBack()
            return null
        }
        var number = ""
        if (actionView.hasAction()) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_PLACES_CALL
            } else {
                Reminder.BY_PLACES_SMS
            }
        }

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
            Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        } else {
            reminder.eventTime = ""
            reminder.startTime = ""
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_place

    override fun provideViews() {
        setViews(
                scrollView = scrollView,
                expansionLayout = moreLayout,
                ledPickerView = ledView,
                extraView = tuneExtraView,
                melodyView = melodyView,
                attachmentView = attachmentView,
                groupView = groupView,
                summaryView = taskSummary,
                dateTimeView = dateView,
                loudnessPickerView = loudnessView,
                priorityPickerView = priorityView,
                windowTypeView = windowTypeView,
                actionView = actionView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isTablet()) {
            mapContainer.visibility = View.GONE
            mapButton.visibility = View.VISIBLE
        } else {
            mapContainer.visibility = View.VISIBLE
            mapButton.visibility = View.GONE
        }

        val placesMap = PlacesMapFragment()
        placesMap.setListener(mListener)
        placesMap.setCallback(object : MapCallback {
            override fun onMapReady() {
                mPlacesMap?.selectMarkers(iFace.state.reminder.places)
            }
        })
        placesMap.markerRadius = prefs.radius
        placesMap.setMarkerStyle(prefs.markerStyle)
        fragmentManager?.beginTransaction()
                ?.replace(mapFrame.id, placesMap)
                ?.addToBackStack(null)
                ?.commit()
        this.mPlacesMap = placesMap

        tuneExtraView.hasAutoExtra = false

        delayLayout.visibility = View.GONE
        attackDelay.setOnCheckedChangeListener { _, isChecked ->
            iFace.state.isDelayAdded = isChecked
            if (isChecked) {
                delayLayout.visibility = View.VISIBLE
            } else {
                delayLayout.visibility = View.GONE
            }
        }
        attackDelay.isChecked = iFace.state.isDelayAdded
        mapButton.setOnClickListener { toggleMap() }
        editReminder()
    }

    override fun updateActions() {
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

    private fun toggleMap() {
        if (!isTablet()) {
            if (mapContainer != null && mapContainer.visibility == View.VISIBLE) {
                ViewUtils.fadeOutAnimation(mapContainer)
                ViewUtils.fadeInAnimation(scrollView)
            } else {
                ViewUtils.fadeOutAnimation(scrollView)
                ViewUtils.fadeInAnimation(mapContainer)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return mPlacesMap == null || mPlacesMap!!.onBackPressed()
    }

    private fun editReminder() {
        val reminder = iFace.state.reminder
        Timber.d("editReminder: %s", reminder)
        if (reminder.eventTime != "" && reminder.hasReminder) {
            dateView.setDateTime(reminder.eventTime)
            attackDelay.isChecked = true
        }
    }
}
